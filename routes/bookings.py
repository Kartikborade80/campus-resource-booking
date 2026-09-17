from flask import Blueprint, render_template, request, redirect, url_for, flash, session, jsonify
from routes.auth import login_required, role_required
from db import query_one, query_all, execute_action, get_db_connection
import datetime

bookings_bp = Blueprint('bookings', __name__)

@bookings_bp.route('/bookings')
@login_required
def index():
    user_id = session['user_id']
    role = session['role']

    search = request.args.get('search', '').strip()
    status_filter = request.args.get('status', '').strip()
    date_filter = request.args.get('date', '').strip()
    dept_filter = request.args.get('department_id', '').strip()

    sql = """
        SELECT 
            b.booking_id,
            b.booked_by,
            b.user_role,
            b.user_email,
            b.department_name,
            b.department_code,
            b.resource_name,
            b.resource_code,
            b.resource_category,
            b.room_number,
            b.building_name,
            b.room_type,
            b.slot_name,
            b.start_time,
            b.end_time,
            b.booking_date,
            b.purpose,
            b.booking_status,
            b.approved_by_name,
            b.created_at,
            orig.user_id AS owner_user_id
        FROM booking_details_view b
        JOIN bookings orig ON b.booking_id = orig.booking_id
        WHERE 1=1
    """
    params = []

    if role not in ('Admin', 'Staff'):
        sql += " AND orig.user_id = %s"
        params.append(user_id)

    if search:
        sql += " AND (b.booked_by LIKE %s OR b.resource_name LIKE %s OR b.purpose LIKE %s OR b.room_number LIKE %s)"
        like_search = f"%{search}%"
        params.extend([like_search, like_search, like_search, like_search])

    if status_filter:
        sql += " AND b.booking_status = %s"
        params.append(status_filter)

    if date_filter:
        sql += " AND b.booking_date = %s"
        params.append(date_filter)

    if dept_filter and role in ('Admin', 'Staff'):
        sql += " AND orig.user_id IN (SELECT user_id FROM users WHERE department_id = %s)"
        params.append(dept_filter)

    sql += " ORDER BY b.booking_date DESC, b.booking_id DESC"

    bookings = query_all(sql, params)
    departments = query_all("SELECT department_id, department_name, department_code FROM departments WHERE status = 'Active'")

    # Segregate Pending and Approved requests for labs & classrooms
    pending_bookings = [b for b in bookings if str(b.get('booking_status', '')).upper() == 'PENDING']
    approved_bookings = [b for b in bookings if str(b.get('booking_status', '')).upper() == 'APPROVED']

    active_tab = request.args.get('tab', '').strip().lower()
    if not active_tab:
        if status_filter:
            active_tab = 'all'
        elif pending_bookings:
            active_tab = 'pending'
        else:
            active_tab = 'approved' if approved_bookings else 'all'

    return render_template(
        'bookings/list.html',
        bookings=bookings,
        pending_bookings=pending_bookings,
        approved_bookings=approved_bookings,
        pending_count=len(pending_bookings),
        approved_count=len(approved_bookings),
        active_tab=active_tab,
        departments=departments,
        search=search,
        selected_status=status_filter,
        selected_date=date_filter,
        selected_dept=dept_filter
    )

@bookings_bp.route('/bookings/new', methods=['GET', 'POST'])
@login_required
def new():
    if request.method == 'POST':
        user_id = session['user_id']
        resource_id = request.form.get('resource_id')
        room_id = request.form.get('room_id') or None
        slot_id = request.form.get('slot_id')
        booking_date = request.form.get('booking_date')
        purpose = request.form.get('purpose', '').strip()

        if not resource_id or not slot_id or not booking_date or not purpose:
            flash('Please specify Resource, Time Slot, Booking Date, and Purpose.', 'danger')
            return redirect(url_for('bookings.new'))

        today_str = datetime.date.today().strftime('%Y-%m-%d')
        if booking_date < today_str:
            flash('Bookings cannot be scheduled for past dates.', 'danger')
            return redirect(url_for('bookings.new'))

        conn = get_db_connection()
        cursor = conn.cursor()
        try:
            cursor.execute("""
                CALL book_resource(%s, %s, %s, %s, %s, %s, @status_code, @message, @new_booking_id);
            """, (user_id, resource_id, room_id, slot_id, booking_date, purpose))

            cursor.execute("SELECT @status_code AS code, @message AS msg, @new_booking_id AS booking_id;")
            res = cursor.fetchone()
            conn.commit()

            if res and res['code'] == 0:
                flash(f"Success! {res['msg']} (Booking Reference: #{res['booking_id']})", 'success')
                return redirect(url_for('bookings.index', tab='pending'))
            else:
                error_msg = res['msg'] if res else 'Unknown booking error occurred.'
                flash(f"Booking Failed: {error_msg}", 'danger')

        except Exception as ex:
            conn.rollback()
            flash(f"Database error: {ex}", 'danger')
        finally:
            cursor.close()
            conn.close()

    resources = query_all("""
        SELECT r.resource_id, r.resource_name, r.resource_code, r.location, r.status, c.category_name
        FROM resources r
        JOIN resource_categories c ON r.category_id = c.category_id
        WHERE r.status <> 'Inactive'
        ORDER BY c.category_name, r.resource_name
    """)

    rooms = query_all("""
        SELECT room_id, room_number, building_name, room_type, capacity, status
        FROM rooms
        WHERE status <> 'Inactive'
        ORDER BY building_name, room_number
    """)

    slots = query_all("""
        SELECT slot_id, slot_name, TIME_FORMAT(start_time, '%%h:%%i %%p') AS start_fmt,
               TIME_FORMAT(end_time, '%%h:%%i %%p') AS end_fmt
        FROM time_slots
        WHERE status = 'Active'
        ORDER BY start_time ASC
    """)

    today = datetime.date.today().strftime('%Y-%m-%d')
    return render_template('bookings/new.html', resources=resources, rooms=rooms, slots=slots, today=today)

@bookings_bp.route('/bookings/approve/<int:booking_id>', methods=['POST'])
@login_required
@role_required('Admin')
def approve(booking_id):
    try:
        execute_action("""
            UPDATE bookings
            SET booking_status = 'APPROVED', approved_by = %s
            WHERE booking_id = %s
        """, (session['user_id'], booking_id))
        flash(f'Booking #{booking_id} has been Approved! Venue and equipment reserved.', 'success')
    except Exception as e:
        flash(f'Error approving booking: {e}', 'danger')
    return redirect(url_for('bookings.index', tab='approved'))

@bookings_bp.route('/bookings/reject/<int:booking_id>', methods=['POST'])
@login_required
@role_required('Admin')
def reject(booking_id):
    try:
        execute_action("""
            UPDATE bookings
            SET booking_status = 'REJECTED', approved_by = %s
            WHERE booking_id = %s
        """, (session['user_id'], booking_id))
        flash(f'Booking #{booking_id} has been Rejected.', 'info')
    except Exception as e:
        flash(f'Error rejecting booking: {e}', 'danger')
    return redirect(url_for('bookings.index', tab='pending'))

@bookings_bp.route('/bookings/cancel/<int:booking_id>', methods=['POST'])
@login_required
def cancel(booking_id):
    user_id = session['user_id']

    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("CALL cancel_booking(%s, %s, @status_code, @message);", (booking_id, user_id))
        cursor.execute("SELECT @status_code AS code, @message AS msg;")
        res = cursor.fetchone()
        conn.commit()

        if res and res['code'] == 0:
            flash(res['msg'], 'success')
        else:
            flash(res['msg'] if res else 'Unable to cancel booking.', 'danger')
    except Exception as ex:
        conn.rollback()
        flash(f"Error: {ex}", 'danger')
    finally:
        cursor.close()
        conn.close()

    return redirect(url_for('bookings.index'))

@bookings_bp.route('/bookings/complete/<int:booking_id>', methods=['POST'])
@login_required
@role_required('Admin', 'Staff')
def complete(booking_id):
    try:
        execute_action("""
            UPDATE bookings
            SET booking_status = 'COMPLETED'
            WHERE booking_id = %s
        """, (booking_id,))
        flash(f'Booking #{booking_id} marked as Completed. Resource released back to Available.', 'success')
    except Exception as e:
        flash(f'Error completing booking: {e}', 'danger')
    return redirect(url_for('bookings.index', tab='approved'))

@bookings_bp.route('/api/check-availability')
@login_required
def check_availability():
    date = request.args.get('date')
    slot_id = request.args.get('slot_id')
    resource_id = request.args.get('resource_id')
    room_id = request.args.get('room_id')

    if not date or not slot_id or not resource_id:
        return jsonify({'available': False, 'message': 'Date, resource, and time slot are required.'})

    res_conflict = query_one("""
        SELECT b.booking_id, u.name AS booked_by
        FROM bookings b
        JOIN users u ON b.user_id = u.user_id
        WHERE b.resource_id = %s AND b.booking_date = %s AND b.slot_id = %s
          AND b.booking_status IN ('Pending', 'Approved')
    """, (resource_id, date, slot_id))

    if res_conflict:
        return jsonify({
            'available': False,
            'message': f"Resource is already reserved by {res_conflict['booked_by']} for this slot."
        })

    if room_id and room_id.isdigit() and int(room_id) > 0:
        room_conflict = query_one("""
            SELECT b.booking_id, u.name AS booked_by, rm.room_number
            FROM bookings b
            JOIN users u ON b.user_id = u.user_id
            JOIN rooms rm ON b.room_id = rm.room_id
            WHERE b.room_id = %s AND b.booking_date = %s AND b.slot_id = %s
              AND b.booking_status IN ('Pending', 'Approved')
        """, (room_id, date, slot_id))

        if room_conflict:
            return jsonify({
                'available': False,
                'message': f"Room {room_conflict['room_number']} is already booked by {room_conflict['booked_by']} for this slot."
            })

    return jsonify({'available': True, 'message': 'Resource and room are available for this time slot!'})
