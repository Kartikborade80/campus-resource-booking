from flask import Blueprint, render_template, request, redirect, url_for, flash
from routes.auth import login_required, role_required
from db import query_one, query_all, execute_action

rooms_bp = Blueprint('rooms', __name__)

@rooms_bp.route('/rooms')
@login_required
def index():
    search = request.args.get('search', '').strip()
    building = request.args.get('building', '').strip()
    room_type = request.args.get('room_type', '').strip()
    min_capacity = request.args.get('min_capacity', '').strip()

    sql = """
        SELECT r.room_id, r.room_number, r.building_name, r.floor_number,
               r.capacity, r.room_type, r.facilities, r.status,
               (SELECT COUNT(*) FROM bookings b WHERE b.room_id = r.room_id) AS total_bookings
        FROM rooms r
        WHERE 1=1
    """
    params = []

    if search:
        sql += " AND (r.room_number LIKE %s OR r.facilities LIKE %s OR r.building_name LIKE %s)"
        like_search = f"%{search}%"
        params.extend([like_search, like_search, like_search])

    if building:
        sql += " AND r.building_name = %s"
        params.append(building)

    if room_type:
        sql += " AND r.room_type = %s"
        params.append(room_type)

    if min_capacity and min_capacity.isdigit():
        sql += " AND r.capacity >= %s"
        params.append(int(min_capacity))

    sql += " ORDER BY r.building_name ASC, r.floor_number ASC, r.room_number ASC"

    rooms = query_all(sql, params)
    buildings = query_all("SELECT DISTINCT building_name FROM rooms ORDER BY building_name")
    room_types = ['Classroom', 'Computer Lab', 'Seminar Hall', 'Auditorium', 'Conference Room']

    return render_template(
        'rooms/list.html',
        rooms=rooms,
        buildings=buildings,
        room_types=room_types,
        search=search,
        selected_building=building,
        selected_room_type=room_type,
        min_capacity=min_capacity
    )

@rooms_bp.route('/rooms/create', methods=['POST'])
@login_required
@role_required('Admin')
def create():
    room_number = request.form.get('room_number', '').strip()
    building_name = request.form.get('building_name', '').strip()
    floor_number = request.form.get('floor_number', 0)
    capacity = request.form.get('capacity', 30)
    room_type = request.form.get('room_type')
    facilities = request.form.get('facilities', '').strip()
    status = request.form.get('status', 'Available')

    if not room_number or not building_name or not room_type:
        flash('Room Number, Building Name, and Room Type are required.', 'danger')
        return redirect(url_for('rooms.index'))

    existing = query_one("SELECT room_id FROM rooms WHERE UPPER(room_number) = %s", (room_number.upper(),))
    if existing:
        flash(f"Room '{room_number}' already exists.", 'danger')
        return redirect(url_for('rooms.index'))

    try:
        execute_action("""
            INSERT INTO rooms (room_number, building_name, floor_number, capacity, room_type, facilities, status)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
        """, (room_number, building_name, int(floor_number), int(capacity), room_type, facilities or None, status))
        flash(f"Room '{room_number}' registered successfully!", 'success')
    except Exception as e:
        flash(f"Error creating room: {e}", 'danger')

    return redirect(url_for('rooms.index'))

@rooms_bp.route('/rooms/update/<int:room_id>', methods=['POST'])
@login_required
@role_required('Admin')
def update(room_id):
    room_number = request.form.get('room_number', '').strip()
    building_name = request.form.get('building_name', '').strip()
    floor_number = request.form.get('floor_number', 0)
    capacity = request.form.get('capacity', 30)
    room_type = request.form.get('room_type')
    facilities = request.form.get('facilities', '').strip()
    status = request.form.get('status', 'Available')

    existing = query_one("SELECT room_id FROM rooms WHERE UPPER(room_number) = %s AND room_id <> %s", (room_number.upper(), room_id))
    if existing:
        flash(f"Room number '{room_number}' is already taken.", 'danger')
        return redirect(url_for('rooms.index'))

    try:
        execute_action("""
            UPDATE rooms
            SET room_number = %s, building_name = %s, floor_number = %s,
                capacity = %s, room_type = %s, facilities = %s, status = %s
            WHERE room_id = %s
        """, (room_number, building_name, int(floor_number), int(capacity), room_type, facilities or None, status, room_id))
        flash(f"Room '{room_number}' updated successfully!", 'success')
    except Exception as e:
        flash(f"Error updating room: {e}", 'danger')

    return redirect(url_for('rooms.index'))

@rooms_bp.route('/rooms/delete/<int:room_id>', methods=['POST'])
@login_required
@role_required('Admin')
def delete(room_id):
    bookings_cnt = query_one("SELECT COUNT(*) AS cnt FROM bookings WHERE room_id = %s", (room_id,))
    if bookings_cnt and bookings_cnt['cnt'] > 0:
        execute_action("UPDATE rooms SET status = 'Inactive' WHERE room_id = %s", (room_id,))
        flash(f"Room has {bookings_cnt['cnt']} historical bookings. Status safely changed to 'Inactive'.", 'warning')
        return redirect(url_for('rooms.index'))

    try:
        execute_action("DELETE FROM rooms WHERE room_id = %s", (room_id,))
        flash('Room permanently deleted.', 'success')
    except Exception as e:
        flash(f"Error deleting room: {e}", 'danger')

    return redirect(url_for('rooms.index'))
