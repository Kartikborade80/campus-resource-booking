import os
import sys
import argparse
import pymysql
from dotenv import load_dotenv

load_dotenv()

def parse_sql_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    statements = []
    current_delimiter = ';'
    buffer = []

    lines = content.splitlines()
    for line in lines:
        stripped = line.strip()
        
        if stripped.upper().startswith('DELIMITER '):
            parts = stripped.split()
            if len(parts) >= 2:
                current_delimiter = parts[1]
            continue
        
        if not buffer and (stripped.startswith('--') or stripped.startswith('/*')):
            continue
        
        buffer.append(line)
        joined = "\n".join(buffer)

        if stripped.endswith(current_delimiter):
            stmt = joined[: -len(current_delimiter)].strip()
            if stmt:
                statements.append(stmt)
            buffer = []

    if buffer:
        remaining = "\n".join(buffer).strip()
        if remaining:
            statements.append(remaining)

    return statements

def run_setup(host, port, user, password, db_name):
    print(f"Connecting to MySQL at {host}:{port} as user '{user}'...")

    try:
        conn = pymysql.connect(
            host=host,
            port=port,
            user=user,
            password=password,
            charset='utf8mb4',
            autocommit=True
        )
        print("Connected to MySQL server successfully.")
    except Exception as e:
        print(f"Connection failed: {e}")
        return False

    cursor = conn.cursor()
    cursor.execute(f"CREATE DATABASE IF NOT EXISTS `{db_name}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    cursor.execute(f"USE `{db_name}`;")

    sql_files = [
        os.path.join('database', 'schema.sql'),
        os.path.join('database', 'views.sql'),
        os.path.join('database', 'triggers.sql'),
        os.path.join('database', 'procedures.sql'),
    ]

    for rel_path in sql_files:
        full_path = os.path.abspath(rel_path)
        if not os.path.exists(full_path):
            continue

        print(f"Executing: {rel_path}...")
        statements = parse_sql_file(full_path)

        for stmt in statements:
            cleaned = stmt.strip()
            if not cleaned or cleaned.startswith('--'):
                continue
            try:
                cursor.execute(cleaned)
            except Exception as ex:
                print(f"Notice: {ex}")

    cursor.execute("SHOW TABLES;")
    tables = [row[0] for row in cursor.fetchall()]
    print(f"Found {len(tables)} tables & views in `{db_name}`.")

    cursor.close()
    conn.close()

    try:
        with open('db.properties', 'w', encoding='utf-8') as pf:
            pf.write(f"db.url=jdbc:mysql://{host}:{port}/{db_name}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC\n")
            pf.write(f"db.user={user}\n")
            pf.write(f"db.password={password}\n")
    except Exception:
        pass

    return True

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--host', default='127.0.0.1')
    parser.add_argument('--port', type=int, default=3306)
    parser.add_argument('--user', default='root')
    parser.add_argument('--password', default='Pass@123')
    parser.add_argument('--db', default='campus_resource_booking')

    args = parser.parse_args()
    success = run_setup(args.host, args.port, args.user, args.password, args.db)
    if not success:
        sys.exit(1)
