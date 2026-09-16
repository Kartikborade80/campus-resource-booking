from flask import Blueprint, render_template, request, redirect, url_for, flash
from routes.auth import login_required, role_required
from db import query_one, query_all, execute_action

departments_bp = Blueprint('departments', __name__)

@departments_bp.route('/departments')
@login_required
def index():
    departments = query_all("""
        SELECT d.department_id, d.department_name, d.department_code, d.status,
               COUNT(DISTINCT u.user_id) AS total_users,
               COUNT(DISTINCT b.booking_id) AS total_bookings
        FROM departments d
        LEFT JOIN users u ON d.department_id = u.department_id
        LEFT JOIN bookings b ON u.user_id = b.user_id
        GROUP BY d.department_id, d.department_name, d.department_code, d.status
        ORDER BY d.department_id ASC
    """)
    return render_template('departments/list.html', departments=departments)

@departments_bp.route('/departments/create', methods=['POST'])
@login_required
@role_required('Admin')
def create():
    name = request.form.get('department_name', '').strip()
    code = request.form.get('department_code', '').strip().upper()
    status = request.form.get('status', 'Active')

    if not name or not code:
        flash('Department name and unique code are required.', 'danger')
        return redirect(url_for('departments.index'))

    existing = query_one("""
        SELECT department_id FROM departments 
        WHERE LOWER(department_name) = %s OR UPPER(department_code) = %s
    """, (name.lower(), code))

    if existing:
        flash('Department name or code already exists in the system.', 'danger')
        return redirect(url_for('departments.index'))

    try:
        execute_action("""
            INSERT INTO departments (department_name, department_code, status)
            VALUES (%s, %s, %s)
        """, (name, code, status))
        flash(f"Department '{name}' ({code}) created successfully!", 'success')
    except Exception as e:
        flash(f"Error creating department: {e}", 'danger')

    return redirect(url_for('departments.index'))

@departments_bp.route('/departments/update/<int:department_id>', methods=['POST'])
@login_required
@role_required('Admin')
def update(department_id):
    name = request.form.get('department_name', '').strip()
    code = request.form.get('department_code', '').strip().upper()
    status = request.form.get('status', 'Active')

    if not name or not code:
        flash('Department name and code are required.', 'danger')
        return redirect(url_for('departments.index'))

    existing = query_one("""
        SELECT department_id FROM departments 
        WHERE (LOWER(department_name) = %s OR UPPER(department_code) = %s) 
          AND department_id <> %s
    """, (name.lower(), code, department_id))

    if existing:
        flash('Another department already uses this name or code.', 'danger')
        return redirect(url_for('departments.index'))

    try:
        execute_action("""
            UPDATE departments
            SET department_name = %s, department_code = %s, status = %s
            WHERE department_id = %s
        """, (name, code, status, department_id))
        flash(f"Department '{name}' updated successfully!", 'success')
    except Exception as e:
        flash(f"Error updating department: {e}", 'danger')

    return redirect(url_for('departments.index'))

@departments_bp.route('/departments/delete/<int:department_id>', methods=['POST'])
@login_required
@role_required('Admin')
def delete(department_id):
    users_count = query_one("SELECT COUNT(*) AS cnt FROM users WHERE department_id = %s", (department_id,))
    if users_count and users_count['cnt'] > 0:
        flash(f"Cannot delete department: {users_count['cnt']} users are assigned to it. Deactivate instead.", 'warning')
        return redirect(url_for('departments.index'))

    try:
        execute_action("DELETE FROM departments WHERE department_id = %s", (department_id,))
        flash('Department deleted successfully.', 'success')
    except Exception as e:
        flash(f"Error deleting department: {e}", 'danger')

    return redirect(url_for('departments.index'))
