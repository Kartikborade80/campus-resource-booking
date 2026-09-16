from flask import Blueprint, render_template, request, redirect, url_for, flash
from werkzeug.security import generate_password_hash
from routes.auth import login_required, role_required
from db import query_one, query_all, execute_action

users_bp = Blueprint('users', __name__)

@users_bp.route('/users')
@login_required
@role_required('Admin')
def index():
    search = request.args.get('search', '').strip()
    role_filter = request.args.get('role', '').strip()
    dept_filter = request.args.get('department_id', '').strip()

    sql = """
        SELECT u.user_id, u.name, u.email, u.role, u.phone, u.status, u.created_at,
               d.department_name, d.department_code, d.department_id,
               (SELECT COUNT(*) FROM bookings b WHERE b.user_id = u.user_id) AS booking_count
        FROM users u
        JOIN departments d ON u.department_id = d.department_id
        WHERE 1=1
    """
    params = []

    if search:
        sql += " AND (u.name LIKE %s OR u.email LIKE %s OR u.phone LIKE %s)"
        like_search = f"%{search}%"
        params.extend([like_search, like_search, like_search])

    if role_filter:
        sql += " AND u.role = %s"
        params.append(role_filter)

    if dept_filter:
        sql += " AND u.department_id = %s"
        params.append(dept_filter)

    sql += " ORDER BY u.user_id ASC"

    users = query_all(sql, params)
    departments = query_all("SELECT department_id, department_name, department_code FROM departments WHERE status = 'Active'")

    return render_template('users/list.html', users=users, departments=departments, search=search, role_filter=role_filter, dept_filter=dept_filter)

@users_bp.route('/users/create', methods=['POST'])
@login_required
@role_required('Admin')
def create():
    name = request.form.get('name', '').strip()
    email = request.form.get('email', '').strip().lower()
    password = request.form.get('password', '')
    role = request.form.get('role', 'Student')
    department_id = request.form.get('department_id')
    phone = request.form.get('phone', '').strip()
    status = request.form.get('status', 'Active')

    if not name or not email or not password or not department_id:
        flash('All required fields (Name, Email, Password, Department) must be filled.', 'danger')
        return redirect(url_for('users.index'))

    existing = query_one("SELECT user_id FROM users WHERE LOWER(email) = %s", (email,))
    if existing:
        flash(f"User with email '{email}' already exists.", 'danger')
        return redirect(url_for('users.index'))

    hashed = generate_password_hash(password)
    try:
        execute_action("""
            INSERT INTO users (name, email, password_hash, role, department_id, phone, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """, (name, email, hashed, role, department_id, phone or None, status))
        flash(f"User '{name}' added successfully!", 'success')
    except Exception as e:
        flash(f"Error creating user: {e}", 'danger')

    return redirect(url_for('users.index'))

@users_bp.route('/users/update/<int:user_id>', methods=['POST'])
@login_required
@role_required('Admin')
def update(user_id):
    name = request.form.get('name', '').strip()
    email = request.form.get('email', '').strip().lower()
    role = request.form.get('role')
    department_id = request.form.get('department_id')
    phone = request.form.get('phone', '').strip()
    status = request.form.get('status')
    password = request.form.get('password', '').strip()

    if not name or not email or not department_id:
        flash('Name, email, and department are required.', 'danger')
        return redirect(url_for('users.index'))

    existing = query_one("SELECT user_id FROM users WHERE LOWER(email) = %s AND user_id <> %s", (email, user_id))
    if existing:
        flash(f"Email '{email}' is already in use by another user.", 'danger')
        return redirect(url_for('users.index'))

    try:
        if password:
            hashed = generate_password_hash(password)
            execute_action("""
                UPDATE users
                SET name = %s, email = %s, password_hash = %s, role = %s,
                    department_id = %s, phone = %s, status = %s
                WHERE user_id = %s
            """, (name, email, hashed, role, department_id, phone or None, status, user_id))
        else:
            execute_action("""
                UPDATE users
                SET name = %s, email = %s, role = %s,
                    department_id = %s, phone = %s, status = %s
                WHERE user_id = %s
            """, (name, email, role, department_id, phone or None, status, user_id))
        flash(f"User '{name}' updated successfully!", 'success')
    except Exception as e:
        flash(f"Error updating user: {e}", 'danger')

    return redirect(url_for('users.index'))

@users_bp.route('/users/delete/<int:user_id>', methods=['POST'])
@login_required
@role_required('Admin')
def delete(user_id):
    bookings_count = query_one("SELECT COUNT(*) AS cnt FROM bookings WHERE user_id = %s", (user_id,))
    if bookings_count and bookings_count['cnt'] > 0:
        execute_action("UPDATE users SET status = 'Inactive' WHERE user_id = %s", (user_id,))
        flash(f"User has {bookings_count['cnt']} associated booking records. Account status has been safely changed to 'Inactive' to preserve database integrity.", 'warning')
        return redirect(url_for('users.index'))

    try:
        execute_action("DELETE FROM users WHERE user_id = %s", (user_id,))
        flash('User permanently deleted successfully.', 'success')
    except Exception as e:
        flash(f"Error deleting user: {e}", 'danger')

    return redirect(url_for('users.index'))
