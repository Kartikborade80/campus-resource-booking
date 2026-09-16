from flask import Blueprint, render_template, request, redirect, url_for, flash, session
from routes.auth import login_required, role_required
from db import query_one, query_all, execute_action
import datetime

maintenance_bp = Blueprint('maintenance', __name__)

@maintenance_bp.route('/maintenance')
@login_required
def index():
    status_filter = request.args.get('status', '').strip()

    sql = """
        SELECT m.maintenance_id, m.issue_title, m.issue_description,
               m.maintenance_date, m.completion_date, m.maintenance_status, m.cost,
               r.resource_name, r.resource_code, r.resource_id,
               rm.room_number, rm.building_name, rm.room_id,
               u.name AS reported_by_name, u.role AS reporter_role
        FROM maintenance m
        LEFT JOIN resources r ON m.resource_id = r.resource_id
        LEFT JOIN rooms rm ON m.room_id = rm.room_id
        JOIN users u ON m.reported_by = u.user_id
        WHERE 1=1
    """
    params = []

    if status_filter:
        sql += " AND m.maintenance_status = %s"
        params.append(status_filter)

    sql += " ORDER BY m.maintenance_date DESC, m.maintenance_id DESC"

    records = query_all(sql, params)
    resources = query_all("SELECT resource_id, resource_name, resource_code FROM resources WHERE status <> 'Inactive'")
    rooms = query_all("SELECT room_id, room_number, building_name FROM rooms WHERE status <> 'Inactive'")

    stats = query_one("""
        SELECT 
            COUNT(*) AS total_tickets,
            SUM(CASE WHEN maintenance_status IN ('Reported', 'In Progress') THEN 1 ELSE 0 END) AS active_tickets,
            SUM(CASE WHEN maintenance_status = 'Completed' THEN 1 ELSE 0 END) AS completed_tickets,
            SUM(cost) AS total_cost
        FROM maintenance
    """)

    today = datetime.date.today().strftime('%Y-%m-%d')
    return render_template('maintenance/list.html', records=records, resources=resources, rooms=rooms, stats=stats, selected_status=status_filter, today=today)

@maintenance_bp.route('/maintenance/create', methods=['POST'])
@login_required
def create():
    reported_by = session['user_id']
    resource_id = request.form.get('resource_id') or None
    room_id = request.form.get('room_id') or None
    issue_title = request.form.get('issue_title', '').strip()
    issue_description = request.form.get('issue_description', '').strip()
    maintenance_date = request.form.get('maintenance_date') or datetime.date.today().strftime('%Y-%m-%d')
    cost = request.form.get('cost', '0.00')

    if not issue_title or not issue_description:
        flash('Issue Title and Detailed Description are required.', 'danger')
        return redirect(url_for('maintenance.index'))

    if not resource_id and not room_id:
        flash('Please associate the maintenance ticket with either a Resource or a Room.', 'danger')
        return redirect(url_for('maintenance.index'))

    try:
        execute_action("""
            INSERT INTO maintenance (resource_id, room_id, reported_by, issue_title, issue_description, maintenance_date, maintenance_status, cost)
            VALUES (%s, %s, %s, %s, %s, %s, 'Reported', %s)
        """, (resource_id, room_id, reported_by, issue_title, issue_description, maintenance_date, float(cost)))
        flash(f"Maintenance ticket '{issue_title}' created successfully.", 'success')
    except Exception as e:
        flash(f"Error creating maintenance ticket: {e}", 'danger')

    return redirect(url_for('maintenance.index'))

@maintenance_bp.route('/maintenance/update/<int:maintenance_id>', methods=['POST'])
@login_required
@role_required('Admin', 'Staff')
def update(maintenance_id):
    status = request.form.get('maintenance_status')
    cost = request.form.get('cost', '0.00')
    completion_date = request.form.get('completion_date') or None

    if status in ('Completed', 'Closed') and not completion_date:
        completion_date = datetime.date.today().strftime('%Y-%m-%d')

    try:
        execute_action("""
            UPDATE maintenance
            SET maintenance_status = %s, cost = %s, completion_date = %s
            WHERE maintenance_id = %s
        """, (status, float(cost), completion_date, maintenance_id))
        flash('Maintenance ticket updated successfully.', 'success')
    except Exception as e:
        flash(f"Error updating maintenance ticket: {e}", 'danger')

    return redirect(url_for('maintenance.index'))

@maintenance_bp.route('/maintenance/close/<int:maintenance_id>', methods=['POST'])
@login_required
@role_required('Admin', 'Staff')
def close(maintenance_id):
    today = datetime.date.today().strftime('%Y-%m-%d')
    try:
        execute_action("""
            UPDATE maintenance
            SET maintenance_status = 'Closed', completion_date = %s
            WHERE maintenance_id = %s
        """, (today, maintenance_id))
        flash(f"Maintenance ticket #{maintenance_id} closed successfully.", 'success')
    except Exception as e:
        flash(f"Error closing ticket: {e}", 'danger')

    return redirect(url_for('maintenance.index'))
