import os
import sys
import datetime
import pymysql
from config import Config

def populate():
    print(f"Connecting to MySQL database '{Config.DB_NAME}' at {Config.DB_HOST}:{Config.DB_PORT}...")
    conn = pymysql.connect(
        host=Config.DB_HOST,
        port=Config.DB_PORT,
        user=Config.DB_USER,
        password=Config.DB_PASSWORD,
        database=Config.DB_NAME,
        charset='utf8mb4',
        autocommit=False
    )
    cursor = conn.cursor()

    try:
        print("\n1. Updating Views with full compatibility columns...")
        cursor.execute("""
            DROP VIEW IF EXISTS booking_details_view;
        """)
        cursor.execute("""
            CREATE VIEW booking_details_view AS
            SELECT 
                b.booking_id,
                u.name AS user_name,
                u.name AS booked_by,
                u.email AS user_email,
                u.role AS user_role,
                d.department_name,
                d.department_code,
                r.resource_name,
                r.resource_code,
                rc.category_name AS resource_category,
                COALESCE(rm.room_number, 'N/A') AS room_number,
                COALESCE(rm.building_name, 'N/A') AS building_name,
                COALESCE(rm.room_type, 'N/A') AS room_type,
                ts.slot_name AS time_slot,
                ts.slot_name,
                ts.start_time,
                ts.end_time,
                b.booking_date,
                b.purpose,
                b.booking_status,
                COALESCE(appr.name, 'Pending Approval') AS approved_by_name,
                b.created_at
            FROM bookings b
            INNER JOIN users u ON b.user_id = u.user_id
            INNER JOIN departments d ON u.department_id = d.department_id
            INNER JOIN resources r ON b.resource_id = r.resource_id
            INNER JOIN resource_categories rc ON r.category_id = rc.category_id
            LEFT JOIN rooms rm ON b.room_id = rm.room_id
            INNER JOIN time_slots ts ON b.slot_id = ts.slot_id
            LEFT JOIN users appr ON b.approved_by = appr.user_id;
        """)
        conn.commit()
        print("   [OK] booking_details_view updated.")

        print("\n2. Configuring Departments...")
        departments_data = [
            ("AIML – Artificial Intelligence and Machine Learning", "AIML", "Active"),
            ("AIDS – Artificial Intelligence and Data Science", "AIDS", "Active"),
            ("Mechanical Engineering", "MECH", "Active"),
            ("Cyber Security", "CYBER", "Active"),
            ("Computer Engineering", "CSE", "Active"),
            ("General Administration", "ADMIN", "Active")
        ]

        dept_map = {}
        for name, code, status in departments_data:
            cursor.execute("SELECT department_id FROM departments WHERE department_code = %s", (code,))
            row = cursor.fetchone()
            if row:
                dept_id = row[0]
                cursor.execute("UPDATE departments SET department_name = %s, status = %s WHERE department_id = %s",
                               (name, status, dept_id))
                dept_map[code] = dept_id
                print(f"   [UPDATED] Department: {name} ({code}) -> ID {dept_id}")
            else:
                cursor.execute("INSERT INTO departments (department_name, department_code, status) VALUES (%s, %s, %s)",
                               (name, code, status))
                dept_id = cursor.lastrowid
                dept_map[code] = dept_id
                print(f"   [CREATED] Department: {name} ({code}) -> ID {dept_id}")
        conn.commit()

        print("\n3. Configuring Standard Academic Time Slots...")
        time_slots_data = [
            ("Morning Session 1 (09:00 - 10:00)", "09:00:00", "10:00:00"),
            ("Morning Session 2 (10:00 - 11:00)", "10:00:00", "11:00:00"),
            ("Mid-Day Session (11:15 - 12:15)", "11:15:00", "12:15:00"),
            ("Noon Session (12:15 - 01:15)", "12:15:00", "13:15:00"),
            ("Afternoon Session 1 (02:00 - 03:00)", "14:00:00", "15:00:00"),
            ("Afternoon Session 2 (03:00 - 04:00)", "15:00:00", "16:00:00"),
            ("Late Afternoon Session (04:00 - 05:00)", "16:00:00", "17:00:00"),
            ("Morning Lab Block (09:00 - 11:00)", "09:00:00", "11:00:00"),
            ("Afternoon Lab Block (02:00 - 04:00)", "14:00:00", "16:00:00"),
            ("Extended Evening Session (05:00 - 06:30)", "17:00:00", "18:30:00")
        ]

        slot_map = {}
        for sname, stime, etime in time_slots_data:
            cursor.execute("SELECT slot_id FROM time_slots WHERE slot_name = %s", (sname,))
            row = cursor.fetchone()
            if row:
                slot_id = row[0]
                cursor.execute("UPDATE time_slots SET start_time = %s, end_time = %s, status = 'Active' WHERE slot_id = %s",
                               (stime, etime, slot_id))
                slot_map[sname] = slot_id
            else:
                cursor.execute("INSERT INTO time_slots (slot_name, start_time, end_time, status) VALUES (%s, %s, %s, 'Active')",
                               (sname, stime, etime))
                slot_id = cursor.lastrowid
                slot_map[sname] = slot_id
                print(f"   [CREATED] Time Slot: {sname} -> ID {slot_id}")
        conn.commit()

        print("\n4. Configuring College Classrooms (Floors 1, 2, 3, 4) and Computer Labs (Floors 3, 4)...")
        bld = "Sanjivani Engineering Complex"
        rooms_data = [
            # Floor 1: 4 Classrooms
            ("CR-101", bld, 1, 75, "Classroom", "Smart Interactive Display (85 inch), 4K Laser Projector, Surround Audio, Central AC, Wi-Fi 6", "AVAILABLE"),
            ("CR-102", bld, 1, 70, "Classroom", "Dual Whiteboards, High-Lumen Full HD Projector, Wireless Microphone, Podium Mic", "AVAILABLE"),
            ("CR-103", bld, 1, 65, "Classroom", "Interactive Flat Panel, Ergonomic Tiered Benches, High-speed Wi-Fi 6, Central AC", "AVAILABLE"),
            ("CR-104", bld, 1, 60, "Classroom", "Smart Digital Board, Hybrid Classroom PTZ Camera Setup, Dual Ceiling Speakers, AC", "AVAILABLE"),

            # Floor 2: 4 Classrooms
            ("CR-201", bld, 2, 75, "Classroom", "Motorized Dual Projection Screens, Digital Touch Podium, Central Audio Amplification", "AVAILABLE"),
            ("CR-202", bld, 2, 70, "Classroom", "Smart Interactive Touch Board, Wireless Screen Cast, Central AC, Podium Setup", "AVAILABLE"),
            ("CR-203", bld, 2, 65, "Classroom", "Ceiling HD Projector, High-Definition Soundbar, Ceramic Whiteboard, Wi-Fi 6", "AVAILABLE"),
            ("CR-204", bld, 2, 60, "Classroom", "Modular Collaborative Desks, 4 Wall-mounted Group Displays, Gigabit LAN Ports", "AVAILABLE"),

            # Floor 3: 4 Classrooms
            ("CR-301", bld, 3, 70, "Classroom", "Smart Interactive Panel, Overhead Laser Projector, Central AC, High-speed Wi-Fi", "AVAILABLE"),
            ("CR-302", bld, 3, 65, "Classroom", "Dual HD Projectors, Smart Podium, Wireless Collar Mic, Air Conditioned", "AVAILABLE"),
            ("CR-303", bld, 3, 65, "Classroom", "Interactive Touch Display, Acoustic Wall Paneling, High-speed LAN & Wi-Fi", "AVAILABLE"),
            ("CR-304", bld, 3, 60, "Classroom", "Full HD Laser Projector, Hybrid Video Conferencing Camera, Central AC", "AVAILABLE"),

            # Floor 3: Reserved Computer Labs
            ("LAB-305", bld, 3, 50, "Computer Lab", "IBM Software Center of Excellence - 50 High-End Workstations (Intel Core i7 13th Gen, 32GB RAM, 1TB NVMe), IBM Cloud Pak, IBM Rational Suite, Dual 27inch Displays, 10Gbps Fiber, Central AC", "AVAILABLE"),
            ("LAB-306", bld, 3, 45, "Computer Lab", "AI & Deep Learning Research Lab - 45 Workstations with Dedicated NVIDIA RTX GPUs, PyTorch, TensorFlow, CUDA Toolkit, JupyterHub Server, 4K Display, Central AC", "AVAILABLE"),
            ("LAB-307", bld, 3, 40, "Computer Lab", "Cisco Advanced Networking & Cyber Security Lab - 40 Dual-NIC Systems, Cisco Catalyst Switches, 2911 Routers Rack, Wireshark, GNS3, Packet Tracer, Firewalls, AC", "AVAILABLE"),
            ("LAB-308", bld, 3, 45, "Computer Lab", "Cloud Computing & Virtualization Lab - 45 Xeon Workstations, VMware ESXi, OpenStack Private Cloud, Docker & Kubernetes Sandboxes, High-speed SAN Storage, AC", "AVAILABLE"),

            # Floor 4: 4 Classrooms (including classrooms on the 4th floor)
            ("CR-401", bld, 4, 75, "Classroom", "Smart Interactive 85inch Board, 4K Laser Projector, Digital Audio System, Central AC, High-speed Wi-Fi 6", "AVAILABLE"),
            ("CR-403", bld, 4, 70, "Classroom", "Interactive Flat Panel, Overhead Sound System, Wireless Mic, Central AC", "AVAILABLE"),
            ("CR-404", bld, 4, 60, "Classroom", "Dual Display Presentation System, Smart Podium, Hybrid Classroom Audio/Video, Air Conditioned", "AVAILABLE"),
            ("CR-405", bld, 4, 60, "Classroom", "Full HD Laser Projector, Motorized Screen, Ceramic Green & White Boards, Central AC", "AVAILABLE"),

            # Floor 4: Reserved Computer Labs
            ("LAB-407", bld, 4, 50, "Computer Lab", "IBM Cloud & Enterprise Systems Lab - 50 Enterprise Workstations (Intel i9, 32GB RAM), IBM Watson Studio, Red Hat OpenShift, IBM Db2, Enterprise Server Access, Central AC", "AVAILABLE"),
            ("LAB-408", bld, 4, 45, "Computer Lab", "NVIDIA High-Performance Computing (HPC) Lab - 45 Workstations with NVIDIA A100/RTX GPUs, Parallel Computing Cluster, Liquid Cooled, OpenMPI, CUDA C++, TensorRT", "AVAILABLE"),
            ("LAB-409", bld, 4, 40, "Computer Lab", "Cyber Forensics & Ethical Hacking Lab - 40 Air-Gapped Workstations, Kali Linux, EnCase Forensic, FTK Imager, Hardware Write Blockers, SIEM Lab Sandbox", "AVAILABLE"),
            ("LAB-410", bld, 4, 40, "Computer Lab", "Robotics, IoT & Embedded Systems Lab - 40 Systems with Embedded Toolchains, Raspberry Pi 5 & Arduino Kits, Digital Storage Oscilloscopes, Keil, Robotic Arms", "AVAILABLE"),
            ("LAB-411", bld, 4, 50, "Computer Lab", "Data Science & Big Data Analytics Lab - 50 Workstations, 8-Node Apache Spark & Hadoop Cluster, Python/R Studio, Tableau Desktop, MongoDB, Central AC", "AVAILABLE")
        ]

        room_map = {}
        for rnum, rbld, rflr, rcap, rtype, rfac, rstat in rooms_data:
            cursor.execute("SELECT room_id FROM rooms WHERE room_number = %s", (rnum,))
            row = cursor.fetchone()
            if row:
                room_id = row[0]
                cursor.execute("""
                    UPDATE rooms
                    SET building_name = %s, floor_number = %s, capacity = %s, room_type = %s, facilities = %s, status = %s
                    WHERE room_id = %s
                """, (rbld, rflr, rcap, rtype, rfac, rstat, room_id))
                room_map[rnum] = room_id
            else:
                cursor.execute("""
                    INSERT INTO rooms (room_number, building_name, floor_number, capacity, room_type, facilities, status)
                    VALUES (%s, %s, %s, %s, %s, %s, %s)
                """, (rnum, rbld, rflr, rcap, rtype, rfac, rstat))
                room_id = cursor.lastrowid
                room_map[rnum] = room_id
                print(f"   [CREATED] Room: {rnum} (Floor {rflr}, {rtype}, Cap {rcap}) -> ID {room_id}")
        conn.commit()

        print("\n5. Configuring Resource Categories & Technical Lab Resources...")
        categories_data = [
            ("Computer Laboratories", "Dedicated state-of-the-art computer laboratories with high-end workstations and software suites"),
            ("Smart Classrooms & Lecture Halls", "Interactive smart lecture halls equipped with digital displays, laser projectors, and acoustics"),
            ("High-Performance Computing & AI", "GPU accelerated computing clusters, AI research workstations, and enterprise cloud suites"),
            ("Audio-Visual & Conference Equipment", "Portable 4K laser projectors, wireless microphone systems, and hybrid conferencing equipment")
        ]

        cat_map = {}
        for cname, cdesc in categories_data:
            cursor.execute("SELECT category_id FROM resource_categories WHERE category_name = %s", (cname,))
            row = cursor.fetchone()
            if row:
                cat_id = row[0]
                cursor.execute("UPDATE resource_categories SET description = %s WHERE category_id = %s", (cdesc, cat_id))
                cat_map[cname] = cat_id
            else:
                cursor.execute("INSERT INTO resource_categories (category_name, description) VALUES (%s, %s)", (cname, cdesc))
                cat_id = cursor.lastrowid
                cat_map[cname] = cat_id
                print(f"   [CREATED] Category: {cname} -> ID {cat_id}")
        conn.commit()

        resources_data = [
            # Floor 3 Labs
            ("IBM Software Center of Excellence Lab", "RES-LAB-IBM305", "Computer Laboratories",
             "50 High-End Workstations, IBM Cloud Pak, IBM Rational Suite, 10Gbps Fiber", 50, "Sanjivani Complex - Floor 3", "2025-08-10"),
            ("AI & Deep Learning Research Lab", "RES-LAB-AI306", "High-Performance Computing & AI",
             "45 RTX GPU Workstations, PyTorch, TensorFlow, CUDA Toolkit, JupyterHub Server", 45, "Sanjivani Complex - Floor 3", "2025-09-01"),
            ("Cisco Advanced Networking & Cyber Security Lab", "RES-LAB-CISCO307", "Computer Laboratories",
             "40 Dual-NIC Workstations, Cisco Catalyst Switches, 2911 Routers Rack, Wireshark", 40, "Sanjivani Complex - Floor 3", "2025-07-15"),
            ("Cloud Computing & Virtualization Lab", "RES-LAB-CLOUD308", "Computer Laboratories",
             "45 Xeon Workstations, VMware ESXi, OpenStack Private Cloud, Docker/Kubernetes", 45, "Sanjivani Complex - Floor 3", "2025-10-05"),

            # Floor 4 Labs
            ("IBM Cloud & Enterprise Systems Lab", "RES-LAB-IBM407", "Computer Laboratories",
             "50 Enterprise Workstations, IBM Watson Studio, Red Hat OpenShift, IBM Db2", 50, "Sanjivani Complex - Floor 4", "2025-08-20"),
            ("NVIDIA High-Performance Computing (HPC) Lab", "RES-LAB-NV408", "High-Performance Computing & AI",
             "45 NVIDIA A100/RTX GPU Workstations, Parallel Computing Cluster, Liquid Cooled", 45, "Sanjivani Complex - Floor 4", "2025-11-12"),
            ("Cyber Forensics & Ethical Hacking Lab", "RES-LAB-CYBER409", "Computer Laboratories",
             "40 Air-Gapped Systems, Kali Linux, EnCase, FTK Imager, Hardware Write Blockers", 40, "Sanjivani Complex - Floor 4", "2025-06-18"),
            ("Robotics, IoT & Embedded Systems Lab", "RES-LAB-IOT410", "Computer Laboratories",
             "40 Systems, Raspberry Pi 5 & Arduino Kits, Oscilloscopes, Keil, Robotic Arms", 40, "Sanjivani Complex - Floor 4", "2025-09-25"),
            ("Data Science & Big Data Analytics Lab", "RES-LAB-DS411", "High-Performance Computing & AI",
             "50 Workstations, 8-Node Apache Spark & Hadoop Cluster, Python/R Studio, Tableau", 50, "Sanjivani Complex - Floor 4", "2025-08-15"),

            # Classrooms
            ("Smart Classroom 101 Venue", "RES-CR-101", "Smart Classrooms & Lecture Halls",
             "Smart Interactive 85inch Display, 4K Laser Projector, Surround Audio System, Central AC", 75, "Sanjivani Complex - Floor 1", "2025-05-10"),
            ("Classroom 102 Lecture Theater", "RES-CR-102", "Smart Classrooms & Lecture Halls",
             "Dual Whiteboards, High-Lumen Projector, Wireless Microphone, Podium", 70, "Sanjivani Complex - Floor 1", "2025-05-12"),
            ("Smart Classroom 201 Venue", "RES-CR-201", "Smart Classrooms & Lecture Halls",
             "Motorized Dual Projection Screens, Digital Touch Podium, Central Audio", 75, "Sanjivani Complex - Floor 2", "2025-05-15"),
            ("Classroom 202 Interactive Room", "RES-CR-202", "Smart Classrooms & Lecture Halls",
             "Smart Interactive Touch Board, Wireless Screen Cast, Central AC", 70, "Sanjivani Complex - Floor 2", "2025-05-15"),
            ("Smart Classroom 301 Venue", "RES-CR-301", "Smart Classrooms & Lecture Halls",
             "Smart Interactive Panel, Overhead Laser Projector, Central AC, High-speed Wi-Fi", 70, "Sanjivani Complex - Floor 3", "2025-05-20"),
            ("Smart Classroom 401 Venue", "RES-CR-401", "Smart Classrooms & Lecture Halls",
             "Smart Interactive 85inch Board, 4K Laser Projector, Digital Audio System, Central AC", 75, "Sanjivani Complex - Floor 4", "2025-05-20"),
            ("Classroom 403 Advanced Theater", "RES-CR-403", "Smart Classrooms & Lecture Halls",
             "Interactive Flat Panel, Overhead Sound System, Wireless Mic, Central AC", 70, "Sanjivani Complex - Floor 4", "2025-05-22")
        ]

        res_map = {}
        for rname, rcode, cname, rdesc, rcap, rloc, pdate in resources_data:
            cat_id = cat_map[cname]
            cursor.execute("SELECT resource_id FROM resources WHERE resource_code = %s", (rcode,))
            row = cursor.fetchone()
            if row:
                res_id = row[0]
                cursor.execute("""
                    UPDATE resources
                    SET category_id = %s, resource_name = %s, description = %s, capacity = %s,
                        location = %s, status = 'AVAILABLE', purchase_date = %s
                    WHERE resource_id = %s
                """, (cat_id, rname, rdesc, rcap, rloc, pdate, res_id))
                res_map[rcode] = res_id
            else:
                cursor.execute("""
                    INSERT INTO resources (category_id, resource_name, resource_code, description, capacity, location, status, purchase_date)
                    VALUES (%s, %s, %s, %s, %s, %s, 'AVAILABLE', %s)
                """, (cat_id, rname, rcode, rdesc, rcap, rloc, pdate))
                res_id = cursor.lastrowid
                res_map[rcode] = res_id
                print(f"   [CREATED] Resource: {rname} ({rcode}) -> ID {res_id}")
        conn.commit()

        print("\n6. Registering Faculty Members with default password 'Pass@123'...")
        faculty_data = [
            # AIML
            ("Dr. Anand R. Joshi", "anand.joshi@sanjivani.edu.in", "+91-9822014521", "AIML"),
            ("Prof. Neha S. Deshmukh", "neha.deshmukh@sanjivani.edu.in", "+91-9823145672", "AIML"),
            ("Dr. Sanjay K. Kulkarni", "sanjay.kulkarni@sanjivani.edu.in", "+91-9850123984", "AIML"),

            # AIDS
            ("Dr. Rajesh M. Patil", "rajesh.patil@sanjivani.edu.in", "+91-9822556781", "AIDS"),
            ("Prof. Priya V. Sharma", "priya.sharma@sanjivani.edu.in", "+91-9890112233", "AIDS"),
            ("Prof. Amit S. Shinde", "amit.shinde@sanjivani.edu.in", "+91-9860334455", "AIDS"),

            # Computer Engineering
            ("Dr. Mahesh B. Gunjal", "mahesh.gunjal@sanjivani.edu.in", "+91-9822441122", "CSE"),
            ("Prof. Sunita M. Sonawane", "sunita.sonawane@sanjivani.edu.in", "+91-9822889900", "CSE"),
            ("Prof. Vikram P. Gaikwad", "vikram.gaikwad@sanjivani.edu.in", "+91-9850776655", "CSE"),

            # Cyber Security
            ("Dr. Sachin V. Gawande", "sachin.gawande@sanjivani.edu.in", "+91-9822337788", "CYBER"),
            ("Prof. Pooja N. Thorat", "pooja.thorat@sanjivani.edu.in", "+91-9890223344", "CYBER"),
            ("Prof. Rohan K. More", "rohan.more@sanjivani.edu.in", "+91-9860889911", "CYBER"),

            # Mechanical Engineering
            ("Dr. Nitin D. Choudhari", "nitin.choudhari@sanjivani.edu.in", "+91-9822667788", "MECH"),
            ("Prof. Sandeep B. Wagh", "sandeep.wagh@sanjivani.edu.in", "+91-9850445566", "MECH"),
            ("Prof. Kavita R. Pawar", "kavita.pawar@sanjivani.edu.in", "+91-9890556677", "MECH")
        ]

        faculty_map = {}
        for fname, femail, fphone, dcode in faculty_data:
            d_id = dept_map[dcode]
            cursor.execute("SELECT user_id FROM users WHERE LOWER(email) = %s", (femail.lower(),))
            row = cursor.fetchone()
            if row:
                u_id = row[0]
                cursor.execute("""
                    UPDATE users
                    SET name = %s, password = 'Pass@123', phone = %s, role = 'FACULTY', department_id = %s, status = 'Active'
                    WHERE user_id = %s
                """, (fname, fphone, d_id, u_id))
                faculty_map[femail] = u_id
                print(f"   [UPDATED] Faculty: {fname} -> ID {u_id}")
            else:
                cursor.execute("""
                    INSERT INTO users (name, email, password, phone, role, department_id, status)
                    VALUES (%s, %s, 'Pass@123', %s, 'FACULTY', %s, 'Active')
                """, (fname, femail.lower(), fphone, d_id))
                u_id = cursor.lastrowid
                faculty_map[femail] = u_id
                print(f"   [REGISTERED] Faculty: {fname} ({femail}) -> ID {u_id}")
        conn.commit()

        print("\n7. Creating Realistic Bookings across Classrooms and Labs...")
        # We will create bookings for completed dates (with feedback) and upcoming dates (approved)
        # Using today's date context
        today = datetime.date.today()
        day_m3 = today - datetime.timedelta(days=3)
        day_m2 = today - datetime.timedelta(days=2)
        day_m1 = today - datetime.timedelta(days=1)
        day_p1 = today + datetime.timedelta(days=1)
        day_p2 = today + datetime.timedelta(days=2)
        day_p3 = today + datetime.timedelta(days=3)
        day_p4 = today + datetime.timedelta(days=4)

        bookings_plan = [
            # Completed bookings with feedback
            {
                "faculty_email": "anand.joshi@sanjivani.edu.in",
                "res_code": "RES-LAB-IBM305",
                "room_num": "LAB-305",
                "slot_name": "Morning Lab Block (09:00 - 11:00)",
                "date": day_m3,
                "purpose": "Final Year AIML: Hands-on Practical Examination on IBM Cloud Pak and Microservices Deployment",
                "status": "COMPLETED",
                "feedback": (5, "State-of-the-art workstations with blazing fast cloud cluster connectivity. All 50 students completed test pipelines seamlessly.")
            },
            {
                "faculty_email": "sachin.gawande@sanjivani.edu.in",
                "res_code": "RES-LAB-CYBER409",
                "room_num": "LAB-409",
                "slot_name": "Afternoon Lab Block (02:00 - 04:00)",
                "date": day_m3,
                "purpose": "Cyber Security Drill: Malware Reverse Engineering & EnCase Forensic Evidence Acquisition Workshop",
                "status": "COMPLETED",
                "feedback": (5, "Air-gapped lab environment worked perfectly for isolated malware analysis. Hardware write blockers functioned smoothly.")
            },
            {
                "faculty_email": "rajesh.patil@sanjivani.edu.in",
                "res_code": "RES-LAB-DS411",
                "room_num": "LAB-411",
                "slot_name": "Morning Lab Block (09:00 - 11:00)",
                "date": day_m2,
                "purpose": "AIDS Semester VI: Distributed Data Ingestion & Analytics Pipeline using Apache Spark and Tableau",
                "status": "COMPLETED",
                "feedback": (5, "Multi-node Spark cluster handled large dataset processing with zero latency. Great projector visibility.")
            },
            {
                "faculty_email": "mahesh.gunjal@sanjivani.edu.in",
                "res_code": "RES-LAB-NV408",
                "room_num": "LAB-408",
                "slot_name": "Afternoon Lab Block (02:00 - 04:00)",
                "date": day_m2,
                "purpose": "Computer Engineering: CUDA C++ Parallel Algorithms & GPU Kernel Matrix Optimization Practical",
                "status": "COMPLETED",
                "feedback": (5, "Superb GPU throughput on RTX cards. Excellent cooling and clean lab infrastructure.")
            },
            {
                "faculty_email": "nitin.choudhari@sanjivani.edu.in",
                "res_code": "RES-LAB-IOT410",
                "room_num": "LAB-410",
                "slot_name": "Morning Session 2 (10:00 - 11:00)",
                "date": day_m1,
                "purpose": "Mechanical Engineering: Microcontroller Interfacing with Industrial Robotic Arms & Stepper Actuators",
                "status": "COMPLETED",
                "feedback": (4, "Good lab setup. Oscilloscopes and kits were well-organized. Request extra soldering wire.")
            },
            {
                "faculty_email": "vikram.gaikwad@sanjivani.edu.in",
                "res_code": "RES-CR-401",
                "room_num": "CR-401",
                "slot_name": "Morning Session 1 (09:00 - 10:00)",
                "date": day_m1,
                "purpose": "Classroom Lecture: Distributed Consensus Algorithms (Raft & Paxos) and Fault Tolerance",
                "status": "COMPLETED",
                "feedback": (5, "Smart display clarity on 4th floor classroom is outstanding. Central AC kept the class comfortable.")
            },

            # Upcoming Approved bookings
            {
                "faculty_email": "priya.sharma@sanjivani.edu.in",
                "res_code": "RES-LAB-IBM407",
                "room_num": "LAB-407",
                "slot_name": "Morning Lab Block (09:00 - 11:00)",
                "date": day_p1,
                "purpose": "IBM Cloud & Watson Studio: Predictive AI Modeling Workshop with Industry Mentor",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "pooja.thorat@sanjivani.edu.in",
                "res_code": "RES-LAB-CISCO307",
                "room_num": "LAB-307",
                "slot_name": "Afternoon Lab Block (02:00 - 04:00)",
                "date": day_p1,
                "purpose": "Cyber Security Practical: Configuring Cisco Hardware Firewalls & Snort Intrusion Detection",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "neha.deshmukh@sanjivani.edu.in",
                "res_code": "RES-LAB-AI306",
                "room_num": "LAB-306",
                "slot_name": "Morning Lab Block (09:00 - 11:00)",
                "date": day_p2,
                "purpose": "AIML Batch A: Fine-tuning Transformer Models (BERT & Vision Transformers) on GPU Clusters",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "sunita.sonawane@sanjivani.edu.in",
                "res_code": "RES-LAB-CLOUD308",
                "room_num": "LAB-308",
                "slot_name": "Afternoon Lab Block (02:00 - 04:00)",
                "date": day_p2,
                "purpose": "Cloud Computing: Multi-stage Docker Containerization & Kubernetes Helm Deployment Lab",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "sanjay.kulkarni@sanjivani.edu.in",
                "res_code": "RES-CR-403",
                "room_num": "CR-403",
                "slot_name": "Mid-Day Session (11:15 - 12:15)",
                "date": day_p3,
                "purpose": "Classroom Session: Computer Vision and Convolutional Neural Networks Architecture Discussion",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "sandeep.wagh@sanjivani.edu.in",
                "res_code": "RES-CR-201",
                "room_num": "CR-201",
                "slot_name": "Afternoon Session 1 (02:00 - 03:00)",
                "date": day_p3,
                "purpose": "Mechanical Engineering: Finite Element Analysis (FEA) Simulation Case Studies",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "amit.shinde@sanjivani.edu.in",
                "res_code": "RES-CR-101",
                "room_num": "CR-101",
                "slot_name": "Morning Session 1 (09:00 - 10:00)",
                "date": day_p4,
                "purpose": "AIDS Lecture: Statistical Inference and Hypothesis Testing for Big Data",
                "status": "APPROVED",
                "feedback": None
            },
            {
                "faculty_email": "rohan.more@sanjivani.edu.in",
                "res_code": "RES-CR-301",
                "room_num": "CR-301",
                "slot_name": "Noon Session (12:15 - 01:15)",
                "date": day_p4,
                "purpose": "Cyber Security Class: Applied Cryptography and Public Key Infrastructure (PKI)",
                "status": "PENDING",
                "feedback": None
            }
        ]

        created_bookings = 0
        for b in bookings_plan:
            u_id = faculty_map[b["faculty_email"]]
            r_id = res_map[b["res_code"]]
            rm_id = room_map[b["room_num"]]
            s_id = slot_map[b["slot_name"]]
            b_date = b["date"]
            purpose = b["purpose"]
            b_stat = b["status"]

            # Check if this exact booking already exists
            cursor.execute("""
                SELECT booking_id FROM bookings
                WHERE resource_id = %s AND booking_date = %s AND slot_id = %s
            """, (r_id, b_date, s_id))
            exist_b = cursor.fetchone()

            if not exist_b:
                appr_by = u_id if b_stat in ('APPROVED', 'COMPLETED') else None
                cursor.execute("""
                    INSERT INTO bookings (user_id, resource_id, room_id, slot_id, booking_date, purpose, booking_status, approved_by)
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                """, (u_id, r_id, rm_id, s_id, b_date, purpose, b_stat, appr_by))
                bk_id = cursor.lastrowid

                # Insert participant
                cursor.execute("""
                    INSERT IGNORE INTO booking_participants (booking_id, user_id, participation_role)
                    VALUES (%s, %s, 'Faculty Lead')
                """, (bk_id, u_id))

                # Feedback if completed
                if b["feedback"]:
                    rating, comment = b["feedback"]
                    cursor.execute("""
                        INSERT IGNORE INTO feedback (booking_id, user_id, rating, comments)
                        VALUES (%s, %s, %s, %s)
                    """, (bk_id, u_id, rating, comment))

                created_bookings += 1
                print(f"   [BOOKED] #{bk_id} - {b['faculty_email'].split('@')[0]} booked {b['res_code']} in {b['room_num']} on {b_date} ({b_stat})")
            else:
                bk_id = exist_b[0]
                print(f"   [EXISTS] Booking #{bk_id} already in place.")

        conn.commit()
        print(f"   Total new bookings created: {created_bookings}")

        print("\n8. Adding Realistic Maintenance Record...")
        m_res_id = res_map["RES-CR-102"]
        m_rm_id = room_map["CR-102"]
        admin_id = list(faculty_map.values())[0]

        cursor.execute("SELECT maintenance_id FROM maintenance WHERE resource_id = %s", (m_res_id,))
        if not cursor.fetchone():
            cursor.execute("""
                INSERT INTO maintenance (resource_id, room_id, reported_by, issue_title, issue_description,
                                        maintenance_date, completion_date, maintenance_status, cost)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (
                m_res_id, m_rm_id, admin_id,
                "Laser Projector Optical Filter Cleaning and Firmware Update",
                "Scheduled quarterly preventative maintenance: optical dust filter replacement and lamp health diagnosis.",
                day_m3, day_m2, "Completed", 1250.00
            ))
            conn.commit()
            print("   [CREATED] Preventative maintenance record logged for CR-102.")

        print("\nAll sample data inserted and committed successfully!")

    except Exception as e:
        conn.rollback()
        print(f"\n[ERROR] Transaction rolled back due to error: {e}")
        raise
    finally:
        cursor.close()
        conn.close()

if __name__ == '__main__':
    populate()
