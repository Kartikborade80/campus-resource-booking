from flask import Blueprint, render_template, request, redirect, url_for, flash, jsonify
from routes.auth import login_required, role_required
from db import query_one, query_all, execute_action

resources_bp = Blueprint('resources', __name__)

@resources_bp.route('/resources')
@login_required
def index():
    search = request.args.get('search', '').strip()
    category_id = request.args.get('category_id', '').strip()
    status_filter = request.args.get('status', '').strip()
    location_filter = request.args.get('location', '').strip()

    sql = """
        SELECT r.resource_id, r.resource_name, r.resource_code, r.description,
               r.capacity, r.location, r.status, r.purchase_date,
               c.category_name, c.category_id,
               (SELECT COUNT(*) FROM bookings b WHERE b.resource_id = r.resource_id) AS total_bookings
        FROM resources r
        JOIN resource_categories c ON r.category_id = c.category_id
        WHERE 1=1
    """
    params = []

    if search:
        sql += " AND (r.resource_name LIKE %s OR r.resource_code LIKE %s OR r.description LIKE %s)"
        like_search = f"%{search}%"
        params.extend([like_search, like_search, like_search])

    if category_id:
        sql += " AND r.category_id = %s"
        params.append(category_id)

    if status_filter:
        sql += " AND r.status = %s"
        params.append(status_filter)

    if location_filter:
        sql += " AND r.location LIKE %s"
        params.append(f"%{location_filter}%")

    sql += " ORDER BY r.resource_id ASC"

    resources = query_all(sql, params)
    categories = query_all("SELECT category_id, category_name FROM resource_categories ORDER BY category_name")

    return render_template(
        'resources/list.html',
        resources=resources,
        categories=categories,
        search=search,
        selected_category=category_id,
        selected_status=status_filter,
        selected_location=location_filter
    )

@resources_bp.route('/resources/create', methods=['POST'])
@login_required
@role_required('Admin')
def create():
    category_id = request.form.get('category_id')
    resource_name = request.form.get('resource_name', '').strip()
    resource_code = request.form.get('resource_code', '').strip().upper()
    description = request.form.get('description', '').strip()
    capacity = request.form.get('capacity', 1)
    location = request.form.get('location', '').strip()
    status = request.form.get('status', 'Available')
    purchase_date = request.form.get('purchase_date') or None

    if not category_id or not resource_name or not resource_code or not location:
        flash('Category, Resource Name, Code, and Location are required.', 'danger')
        return redirect(url_for('resources.index'))

    existing = query_one("SELECT resource_id FROM resources WHERE UPPER(resource_code) = %s", (resource_code,))
    if existing:
        flash(f"Resource with code '{resource_code}' already exists.", 'danger')
        return redirect(url_for('resources.index'))

    try:
        execute_action("""
            INSERT INTO resources (category_id, resource_name, resource_code, description, capacity, location, status, purchase_date)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
        """, (category_id, resource_name, resource_code, description or None, int(capacity), location, status, purchase_date))
        flash(f"Resource '{resource_name}' ({resource_code}) created successfully!", 'success')
    except Exception as e:
        flash(f"Error creating resource: {e}", 'danger')

    return redirect(url_for('resources.index'))

@resources_bp.route('/resources/update/<int:resource_id>', methods=['POST'])
@login_required
@role_required('Admin')
def update(resource_id):
    category_id = request.form.get('category_id')
    resource_name = request.form.get('resource_name', '').strip()
    resource_code = request.form.get('resource_code', '').strip().upper()
    description = request.form.get('description', '').strip()
    capacity = request.form.get('capacity', 1)
    location = request.form.get('location', '').strip()
    status = request.form.get('status', 'Available')
    purchase_date = request.form.get('purchase_date') or None

    existing = query_one("SELECT resource_id FROM resources WHERE UPPER(resource_code) = %s AND resource_id <> %s", (resource_code, resource_id))
    if existing:
        flash(f"Resource code '{resource_code}' already belongs to another item.", 'danger')
        return redirect(url_for('resources.index'))

    try:
        execute_action("""
            UPDATE resources
            SET category_id = %s, resource_name = %s, resource_code = %s,
                description = %s, capacity = %s, location = %s, status = %s, purchase_date = %s
            WHERE resource_id = %s
        """, (category_id, resource_name, resource_code, description or None, int(capacity), location, status, purchase_date, resource_id))
        flash(f"Resource '{resource_name}' updated successfully!", 'success')
    except Exception as e:
        flash(f"Error updating resource: {e}", 'danger')

    return redirect(url_for('resources.index'))

@resources_bp.route('/resources/delete/<int:resource_id>', methods=['POST'])
@login_required
@role_required('Admin')
def delete(resource_id):
    bookings_cnt = query_one("SELECT COUNT(*) AS cnt FROM bookings WHERE resource_id = %s", (resource_id,))
    if bookings_cnt and bookings_cnt['cnt'] > 0:
        execute_action("UPDATE resources SET status = 'Inactive' WHERE resource_id = %s", (resource_id,))
        flash(f"Resource has {bookings_cnt['cnt']} historical booking records. Its status was safely marked 'Inactive'.", 'warning')
        return redirect(url_for('resources.index'))

    try:
        execute_action("DELETE FROM resources WHERE resource_id = %s", (resource_id,))
        flash('Resource permanently deleted.', 'success')
    except Exception as e:
        flash(f"Error deleting resource: {e}", 'danger')

    return redirect(url_for('resources.index'))
