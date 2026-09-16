import pymysql
import pymysql.cursors
from contextlib import contextmanager
from config import Config

def get_db_connection(database=None):
    db_to_use = Config.DB_NAME if database is None else database
    return pymysql.connect(
        host=Config.DB_HOST,
        port=Config.DB_PORT,
        user=Config.DB_USER,
        password=Config.DB_PASSWORD,
        database=db_to_use,
        charset='utf8mb4',
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=True
    )

@contextmanager
def get_db_cursor(commit=False, database=None):
    conn = get_db_connection(database)
    cursor = conn.cursor()
    try:
        yield cursor
        if commit:
            conn.commit()
    except Exception:
        if commit:
            conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()

def query_one(sql, params=None):
    with get_db_cursor() as cursor:
        cursor.execute(sql, params or ())
        return cursor.fetchone()

def query_all(sql, params=None):
    with get_db_cursor() as cursor:
        cursor.execute(sql, params or ())
        return cursor.fetchall()

def execute_action(sql, params=None):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        affected = cursor.execute(sql, params or ())
        last_id = cursor.lastrowid
        conn.commit()
        return {'lastrowid': last_id, 'affected_rows': affected}
    except Exception:
        conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()

def execute_script(sql_script, database=None):
    conn = get_db_connection(database)
    cursor = conn.cursor()
    try:
        statements = sql_script.split(';')
        for stmt in statements:
            cleaned = stmt.strip()
            if cleaned:
                cursor.execute(cleaned)
        conn.commit()
    except Exception:
        conn.rollback()
        raise
    finally:
        cursor.close()
        conn.close()
