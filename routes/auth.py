from flask import Blueprint, render_template, request, redirect, url_for, flash, session
from werkzeug.security import check_password_hash, generate_password_hash
from functools import wraps
from db import query_one, query_all, execute_action

auth_bp = Blueprint('auth', __name__)

def login_required(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        if 'user_id' not in session:
            flash('Please log in to access this page.', 'warning')
            return redirect(url_for('auth.login'))
        return f(*args, **kwargs)
    return decorated_function

def role_required(*roles):
    def decorator(f):
        @wraps(f)
        def decorated_function(*args, **kwargs):
            if 'user_id' not in session:
                flash('Please log in to continue.', 'warning')
                return redirect(url_for('auth.login'))
            if session.get('role') not in roles:
                flash('You do not have permission to perform this action.', 'danger')
                return redirect(url_for('dashboard.index'))
            return f(*args, **kwargs)
        return decorated_function
    return decorator

@auth_bp.route('/login', methods=['GET', 'POST'])
def login():
    if 'user_id' in session:
        return redirect(url_for('dashboard.index'))

    if request.method == 'POST':
        email = request.form.get('email', '').strip().lower()
        password = request.form.get('password', '')

        if not email or not password:
            flash('Please provide both email and password.', 'warning')
            return render_template('auth/login.html')

        user = query_one("""
            SELECT u.user_id, u.name, u.email, u.password, u.role, u.department_id, u.status, d.department_name
            FROM users u
            JOIN departments d ON u.department_id = d.department_id
            WHERE LOWER(u.email) = %s
        """, (email,))

        if not user:
            flash('Invalid email or password.', 'danger')
            return render_template('auth/login.html')

        if user['status'] != 'Active':
            flash('Your account is currently inactive or suspended. Please contact the administrator.', 'danger')
            return render_template('auth/login.html')

        is_authenticated = False
        try:
            if check_password_hash(user['password'], password):
                is_authenticated = True
        except Exception:
            pass
        if not is_authenticated and user['password'] == password:
            is_authenticated = True

        if is_authenticated:
            session['user_id'] = user['user_id']
            session['name'] = user['name']
            session['email'] = user['email']
            session['role'] = user['role']
            session['department_id'] = user['department_id']
            session['department_name'] = user['department_name']

            flash(f"Welcome back, {user['name']}! Logged in as {user['role']}.", 'success')
            return redirect(url_for('dashboard.index'))
        else:
            flash('Invalid email or password.', 'danger')

    departments = query_all("SELECT department_id, department_name, department_code FROM departments WHERE status = 'Active'")
    admin_count = query_one("SELECT COUNT(*) AS cnt FROM users WHERE UPPER(role) = 'ADMIN'")
    has_admin = bool(admin_count and admin_count['cnt'] >= 1)
    return render_template('auth/login.html', departments=departments, has_admin=has_admin)

@auth_bp.route('/register-admin', methods=['POST'])
def register_admin():
    admin_count = query_one("SELECT COUNT(*) AS cnt FROM users WHERE UPPER(role) = 'ADMIN'")
    if admin_count and admin_count['cnt'] >= 1:
        flash('Only one administrator account is permitted in the system. An administrator is already configured.', 'danger')
        return redirect(url_for('auth.login'))

    name = request.form.get('name', '').strip()
    email = request.form.get('email', '').strip().lower()
    password = request.form.get('password', '')
    confirm_password = request.form.get('confirm_password', '')
    department_id = request.form.get('department_id')
    phone = request.form.get('phone', '').strip()
    passcode = request.form.get('passcode', '').strip()

    if not name or not email or not password:
        flash('Name, Email, and Password are required.', 'warning')
        return redirect(url_for('auth.login'))

    if password != confirm_password:
        flash('Passwords do not match.', 'danger')
        return redirect(url_for('auth.login'))

    if passcode != 'ADMIN2026':
        flash('Invalid Security Passcode. Default passcode is ADMIN2026.', 'danger')
        return redirect(url_for('auth.login'))

    existing = query_one("SELECT user_id FROM users WHERE LOWER(email) = %s", (email,))
    if existing:
        flash(f'An account with email {email} already exists.', 'danger')
        return redirect(url_for('auth.login'))

    new_id = execute_action("""
        INSERT INTO users (name, email, password, role, department_id, phone, status)
        VALUES (%s, %s, %s, 'ADMIN', %s, %s, 'Active')
    """, (name, email, password, department_id if department_id else 1, phone))

    if new_id:
        flash(f'Admin account for {name} registered successfully! Please log in.', 'success')
    else:
        flash('Failed to register administrator account.', 'danger')

    return redirect(url_for('auth.login'))


@auth_bp.route('/logout')
def logout():
    name = session.get('name', 'User')
    session.clear()
    flash(f'Goodbye, {name}. You have been securely logged out.', 'info')
    return redirect(url_for('auth.login'))

@auth_bp.route('/profile')
@login_required
def profile():
    user = query_one("""
        SELECT u.user_id, u.name, u.email, u.role, u.phone, u.status, u.created_at,
               d.department_name, d.department_code
        FROM users u
        JOIN departments d ON u.department_id = d.department_id
        WHERE u.user_id = %s
    """, (session['user_id'],))

    recent_bookings = query_all("""
        SELECT b.booking_id, r.resource_name, rm.room_number, ts.slot_name,
               b.booking_date, b.booking_status, b.purpose
        FROM bookings b
        JOIN resources r ON b.resource_id = r.resource_id
        LEFT JOIN rooms rm ON b.room_id = rm.room_id
        JOIN time_slots ts ON b.slot_id = ts.slot_id
        WHERE b.user_id = %s
        ORDER BY b.booking_date DESC, b.booking_id DESC
        LIMIT 5
    """, (session['user_id'],))

    return render_template('auth/profile.html', user=user, recent_bookings=recent_bookings)
