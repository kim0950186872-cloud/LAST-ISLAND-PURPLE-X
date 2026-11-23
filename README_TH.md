# LAST ISLAND PURPLE X v0.2

เครื่องมือ Android สำหรับตรวจสอบไฟล์ APK / OBB / .bytes / JSON แบบอ่านอย่างเดียว

## ความสามารถ
- เลือกไฟล์ผ่าน Android Storage Access Framework
- แสดงชื่อไฟล์ ขนาด และ SHA-256
- อ่านรายการ ZIP/APK ได้สูงสุด 300 รายการ
- ตรวจสอบ JSON แบบข้อความ
- แสดงข้อความ ASCII ที่อ่านได้จากไฟล์ไบนารี
- ไม่ขอสิทธิ์ Android/data หรือ Android/obb

## ข้อควรทราบ
การพบข้อความ เช่น ชื่อฟังก์ชันหรือคลาสในไฟล์ ไม่ได้พิสูจน์ว่าโค้ดนั้นถูกเรียกใช้หรือทำงานจริง
แอปนี้ไม่ถอดรหัสหรือแก้ไข patch, ไม่ inject โค้ด, ไม่เปลี่ยนข้อมูลเกม และไม่หลบเลี่ยง anti-cheat
ไฟล์ APK ที่ผู้ใช้เลือกต้องเป็นไฟล์ที่ผู้ใช้มีสิทธิ์ตรวจสอบ

## สร้าง APK ผ่าน GitHub Actions
1. สร้าง GitHub repository ใหม่
2. อัปโหลดทุกไฟล์และโฟลเดอร์จาก ZIP นี้ โดยคงโครงสร้างเดิม
3. เปิด Actions
4. เลือก Build Android APK แล้วกด Run workflow
5. ดาวน์โหลด Artifact LAST-ISLAND-PURPLE-X-v0.2
6. แตก ZIP แล้วติดตั้ง app-debug.apk
