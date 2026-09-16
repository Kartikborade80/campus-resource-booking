import os
from flask import Flask, render_template, session
from config import Config

from routes.auth import auth_bp
from routes.dashboard import dashboard_bp
from routes.users import users_bp
from routes.departments import departments_bp
from routes.resources import resources_bp
from routes.rooms import rooms_bp
from routes.bookings import bookings_bp
from routes.maintenance import maintenance_bp
from routes.feedback import feedback_bp
from routes.reports import reports_bp

def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    app.register_blueprint(auth_bp)
    app.register_blueprint(dashboard_bp)
    app.register_blueprint(users_bp)
    app.register_blueprint(departments_bp)
    app.register_blueprint(resources_bp)
    app.register_blueprint(rooms_bp)
    app.register_blueprint(bookings_bp)
    app.register_blueprint(maintenance_bp)
    app.register_blueprint(feedback_bp)
    app.register_blueprint(reports_bp)

    @app.context_processor
    def inject_global_data():
        return {
            'current_user': {
                'id': session.get('user_id'),
                'name': session.get('name'),
                'email': session.get('email'),
                'role': session.get('role'),
                'department': session.get('department_name')
            } if 'user_id' in session else None,
            'current_year': 2026,
            'app_name': 'Campus Resource Booking System',
            'college_name': 'Sanjivani University'
        }

    @app.errorhandler(404)
    def page_not_found(e):
        return render_template('404.html'), 404

    @app.errorhandler(500)
    def internal_server_error(e):
        return render_template('500.html', error=e), 500

    return app

app = create_app()

if __name__ == '__main__':
    port = int(os.getenv('PORT', 5000))
    debug = os.getenv('FLASK_DEBUG', 'True').lower() in ('true', '1', 't')
    app.run(host='0.0.0.0', port=port, debug=debug)
