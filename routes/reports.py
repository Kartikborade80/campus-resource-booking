from flask import Blueprint, render_template, request, jsonify
from routes.auth import login_required
from db import query_one, query_all

reports_bp = Blueprint('reports', __name__)

@reports_bp.route('/reports')
@login_required
def index():
    report_type = request.args.get('type', 'most_booked')

    most_booked = query_all("""
        SELECT 
            r.resource_id,
            r.resource_name,
            r.resource_code,
            c.category_name,
            r.location,
            COUNT(b.booking_id) AS total_bookings,
            SUM(CASE WHEN b.booking_status = 'Approved' THEN 1 ELSE 0 END) AS approved_count,
            SUM(CASE WHEN b.booking_status = 'Completed' THEN 1 ELSE 0 END) AS completed_count
        FROM resources r
        JOIN resource_categories c ON r.category_id = c.category_id
        LEFT JOIN bookings b ON r.resource_id = b.resource_id
        GROUP BY r.resource_id, r.resource_name, r.resource_code, c.category_name, r.location
        ORDER BY total_bookings DESC
        LIMIT 10
    """)

    dept_usage = query_all("""
        SELECT 
            d.department_id,
            d.department_name,
            d.department_code,
            COUNT(DISTINCT u.user_id) AS registered_users,
            COUNT(b.booking_id) AS total_bookings,
            SUM(CASE WHEN b.booking_status IN ('Approved', 'Completed') THEN 1 ELSE 0 END) AS successful_bookings
        FROM departments d
        LEFT JOIN users u ON d.department_id = u.department_id
        LEFT JOIN bookings b ON u.user_id = b.user_id
        GROUP BY d.department_id, d.department_name, d.department_code
        ORDER BY total_bookings DESC
    """)

    monthly_stats = query_all("""
        SELECT 
            DATE_FORMAT(booking_date, '%%Y-%%m') AS ym,
            DATE_FORMAT(booking_date, '%%M %%Y') AS month_name,
            COUNT(*) AS total_bookings,
            SUM(CASE WHEN booking_status = 'Completed' THEN 1 ELSE 0 END) AS completed,
            SUM(CASE WHEN booking_status = 'Approved' THEN 1 ELSE 0 END) AS approved,
            SUM(CASE WHEN booking_status = 'Pending' THEN 1 ELSE 0 END) AS pending,
            SUM(CASE WHEN booking_status IN ('Cancelled', 'Rejected') THEN 1 ELSE 0 END) AS cancelled_rejected
        FROM bookings
        GROUP BY ym, month_name
        ORDER BY ym DESC
    """)

    available_resources = query_all("""
        SELECT 
            r.resource_id,
            r.resource_name,
            r.resource_code,
            c.category_name,
            r.capacity,
            r.location,
            r.status,
            r.purchase_date
        FROM resources r
        JOIN resource_categories c ON r.category_id = c.category_id
        WHERE r.status = 'Available'
        ORDER BY c.category_name, r.resource_name
    """)

    maintenance_history = query_all("""
        SELECT 
            m.maintenance_id,
            m.issue_title,
            m.maintenance_date,
            m.completion_date,
            m.maintenance_status,
            m.cost,
            COALESCE(r.resource_name, rm.room_number) AS asset_name,
            u.name AS reported_by_name
        FROM maintenance m
        LEFT JOIN resources r ON m.resource_id = r.resource_id
        LEFT JOIN rooms rm ON m.room_id = rm.room_id
        JOIN users u ON m.reported_by = u.user_id
        ORDER BY m.maintenance_date DESC
    """)

    cancelled_bookings = query_all("""
        SELECT 
            b.booking_id,
            b.booked_by,
            b.user_role,
            b.department_name,
            b.resource_name,
            b.room_number,
            b.slot_name,
            b.booking_date,
            b.booking_status,
            b.purpose
        FROM booking_details_view b
        WHERE b.booking_status IN ('Cancelled', 'Rejected')
        ORDER BY b.booking_date DESC
    """)

    room_utilization = query_all("""
        SELECT 
            room_id,
            room_number,
            building_name,
            room_type,
            capacity,
            room_status,
            total_bookings,
            confirmed_bookings,
            pending_bookings
        FROM room_utilization_view
        ORDER BY total_bookings DESC
    """)

    user_history = query_all("""
        SELECT 
            u.user_id,
            u.name,
            u.email,
            u.role,
            d.department_code,
            COUNT(b.booking_id) AS total_bookings_made,
            SUM(CASE WHEN b.booking_status IN ('Approved', 'Completed') THEN 1 ELSE 0 END) AS confirmed_count,
            MAX(b.booking_date) AS last_booking_date
        FROM users u
        JOIN departments d ON u.department_id = d.department_id
        LEFT JOIN bookings b ON u.user_id = b.user_id
        GROUP BY u.user_id, u.name, u.email, u.role, d.department_code
        ORDER BY total_bookings_made DESC
    """)

    return render_template(
        'reports/index.html',
        report_type=report_type,
        most_booked=most_booked,
        dept_usage=dept_usage,
        monthly_stats=monthly_stats,
        available_resources=available_resources,
        maintenance_history=maintenance_history,
        cancelled_bookings=cancelled_bookings,
        room_utilization=room_utilization,
        user_history=user_history
    )

@reports_bp.route('/api/reports/chart-data')
@login_required
def report_chart_data():
    res_top = query_all("""
        SELECT r.resource_name, COUNT(b.booking_id) AS count
        FROM resources r
        JOIN bookings b ON r.resource_id = b.resource_id
        GROUP BY r.resource_id, r.resource_name
        ORDER BY count DESC
        LIMIT 6
    """)

    dept_top = query_all("""
        SELECT d.department_code, COUNT(b.booking_id) AS count
        FROM departments d
        JOIN users u ON d.department_id = u.department_id
        JOIN bookings b ON u.user_id = b.user_id
        GROUP BY d.department_id, d.department_code
        ORDER BY count DESC
    """)

    room_top = query_all("""
        SELECT room_number, total_bookings
        FROM room_utilization_view
        ORDER BY total_bookings DESC
        LIMIT 6
    """)

    return jsonify({
        'resources': {
            'labels': [r['resource_name'] for r in res_top],
            'data': [r['count'] for r in res_top]
        },
        'departments': {
            'labels': [d['department_code'] for d in dept_top],
            'data': [d['count'] for d in dept_top]
        },
        'rooms': {
            'labels': [rm['room_number'] for rm in room_top],
            'data': [rm['total_bookings'] for rm in room_top]
        }
    })
