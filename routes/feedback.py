from flask import Blueprint, render_template, request, redirect, url_for, flash, session
from routes.auth import login_required
from db import query_one, query_all, execute_action

feedback_bp = Blueprint('feedback', __name__)

@feedback_bp.route('/feedback')
@login_required
def index():
    user_id = session['user_id']

    feedback_list = query_all("""
        SELECT f.feedback_id, f.rating, f.comments, f.submitted_at,
               u.name AS user_name, u.role AS user_role, d.department_name,
               b.booking_id, b.purpose, b.booking_date,
               r.resource_name, rm.room_number
        FROM feedback f
        JOIN bookings b ON f.booking_id = b.booking_id
        JOIN users u ON f.user_id = u.user_id
        JOIN departments d ON u.department_id = d.department_id
        JOIN resources r ON b.resource_id = r.resource_id
        LEFT JOIN rooms rm ON b.room_id = rm.room_id
        ORDER BY f.submitted_at DESC
    """)

    eligible_bookings = query_all("""
        SELECT b.booking_id, b.booking_date, b.purpose, r.resource_name, rm.room_number
        FROM bookings b
        JOIN resources r ON b.resource_id = r.resource_id
        LEFT JOIN rooms rm ON b.room_id = rm.room_id
        WHERE b.user_id = %s
          AND b.booking_status = 'Completed'
          AND b.booking_id NOT IN (SELECT booking_id FROM feedback WHERE user_id = %s)
        ORDER BY b.booking_date DESC
    """, (user_id, user_id))

    stats = query_one("""
        SELECT 
            COUNT(*) AS total_reviews,
            COALESCE(AVG(rating), 0) AS avg_rating,
            SUM(CASE WHEN rating = 5 THEN 1 ELSE 0 END) AS five_star,
            SUM(CASE WHEN rating = 4 THEN 1 ELSE 0 END) AS four_star,
            SUM(CASE WHEN rating = 3 THEN 1 ELSE 0 END) AS three_star,
            SUM(CASE WHEN rating = 2 THEN 1 ELSE 0 END) AS two_star,
            SUM(CASE WHEN rating = 1 THEN 1 ELSE 0 END) AS one_star
        FROM feedback
    """)

    return render_template('feedback/list.html', feedback_list=feedback_list, eligible_bookings=eligible_bookings, stats=stats)

@feedback_bp.route('/feedback/submit', methods=['POST'])
@login_required
def submit():
    user_id = session['user_id']
    booking_id = request.form.get('booking_id')
    rating = request.form.get('rating')
    comments = request.form.get('comments', '').strip()

    if not booking_id or not rating:
        flash('Booking and Rating (1-5 stars) are required.', 'danger')
        return redirect(url_for('feedback.index'))

    try:
        rating_int = int(rating)
        if rating_int < 1 or rating_int > 5:
            flash('Rating must be an integer between 1 and 5.', 'danger')
            return redirect(url_for('feedback.index'))
    except ValueError:
        flash('Invalid rating value.', 'danger')
        return redirect(url_for('feedback.index'))

    already_reviewed = query_one("SELECT feedback_id FROM feedback WHERE booking_id = %s AND user_id = %s", (booking_id, user_id))
    if already_reviewed:
        flash('You have already submitted feedback for this booking.', 'warning')
        return redirect(url_for('feedback.index'))

    try:
        execute_action("""
            INSERT INTO feedback (booking_id, user_id, rating, comments)
            VALUES (%s, %s, %s, %s)
        """, (booking_id, user_id, rating_int, comments or None))
        flash('Thank you! Your feedback has been recorded.', 'success')
    except Exception as e:
        flash(f"Error submitting feedback: {e}", 'danger')

    return redirect(url_for('feedback.index'))
