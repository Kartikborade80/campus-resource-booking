from flask import Blueprint, render_template, jsonify, session
from routes.auth import login_required
from db import query_one, query_all
import datetime

dashboard_bp = Blueprint('dashboard', __name__)

@dashboard_bp.route('/')
@login_required
def index():
    today = datetime.date.today().strftime('%Y-%m-%d')

    stats = query_one("""
        SELECT 
            (SELECT COUNT(*) FROM users WHERE status = 'Active') AS total_users,
            (SELECT COUNT(*) FROM resources WHERE status <> 'Inactive') AS total_resources,
            (SELECT COUNT(*) FROM resources WHERE status = 'Available') AS available_resources,
            (SELECT COUNT(*) FROM rooms WHERE status <> 'Inactive') AS total_rooms,
            (SELECT COUNT(*) FROM bookings WHERE booking_date = %s) AS todays_bookings,
            (SELECT COUNT(*) FROM bookings WHERE booking_status = 'Pending') AS pending_bookings,
            (SELECT COUNT(*) FROM resources WHERE status = 'Maintenance') AS maintenance_resources,
            (SELECT COUNT(*) FROM bookings WHERE booking_status = 'Completed') AS completed_bookings,
            (SELECT COUNT(*) FROM bookings) AS total_bookings
    """, (today,))

    recent_bookings = query_all("""
        SELECT booking_id, booked_by, user_role, department_code, resource_name, 
               room_number, slot_name, booking_date, booking_status, purpose
        FROM booking_details_view
        ORDER BY booking_date DESC, booking_id DESC
        LIMIT 6
    """)

    pending_list = query_all("""
        SELECT booking_id, booked_by, department_name, resource_name, room_number, 
               slot_name, booking_date, purpose
        FROM booking_details_view
        WHERE booking_status = 'Pending'
        ORDER BY booking_date ASC
        LIMIT 5
    """)

    active_maintenance = query_all("""
        SELECT m.maintenance_id, COALESCE(r.resource_name, rm.room_number) AS asset_name,
               m.issue_title, m.maintenance_status, m.maintenance_date, u.name AS reported_by_name
        FROM maintenance m
        LEFT JOIN resources r ON m.resource_id = r.resource_id
        LEFT JOIN rooms rm ON m.room_id = rm.room_id
        JOIN users u ON m.reported_by = u.user_id
        WHERE m.maintenance_status IN ('Reported', 'In Progress')
        ORDER BY m.maintenance_date DESC
        LIMIT 4
    """)

    return render_template(
        'dashboard/index.html',
        stats=stats,
        recent_bookings=recent_bookings,
        pending_list=pending_list,
        active_maintenance=active_maintenance,
        today=today
    )

@dashboard_bp.route('/api/dashboard/charts')
@login_required
def chart_data():
    dept_data = query_all("""
        SELECT d.department_code, COUNT(b.booking_id) AS booking_count
        FROM departments d
        LEFT JOIN users u ON d.department_id = u.department_id
        LEFT JOIN bookings b ON u.user_id = b.user_id
        GROUP BY d.department_id, d.department_code
        ORDER BY booking_count DESC
    """)

    category_data = query_all("""
        SELECT c.category_name, COUNT(b.booking_id) AS total_usage
        FROM resource_categories c
        JOIN resources r ON c.category_id = r.category_id
        LEFT JOIN bookings b ON r.resource_id = b.resource_id
        GROUP BY c.category_id, c.category_name
        ORDER BY total_usage DESC
    """)

    status_data = query_all("""
        SELECT booking_status, COUNT(*) AS status_count
        FROM bookings
        GROUP BY booking_status
    """)

    trend_data = query_all("""
        SELECT 
            DATE_FORMAT(booking_date, '%%b %%Y') AS month_label,
            COUNT(*) AS monthly_count
        FROM bookings
        GROUP BY DATE_FORMAT(booking_date, '%%Y-%%m'), month_label
        ORDER BY MIN(booking_date) ASC
        LIMIT 6
    """)

    return jsonify({
        'departments': {
            'labels': [row['department_code'] for row in dept_data],
            'values': [row['booking_count'] for row in dept_data]
        },
        'categories': {
            'labels': [row['category_name'] for row in category_data],
            'values': [row['total_usage'] for row in category_data]
        },
        'status_distribution': {
            'labels': [row['booking_status'] for row in status_data],
            'values': [row['status_count'] for row in status_data]
        },
        'monthly_trends': {
            'labels': [row['month_label'] for row in trend_data],
            'values': [row['monthly_count'] for row in trend_data]
        }
    })
