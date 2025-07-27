<?php
header("Content-Type: application/json");

// Kết nối MySQL
$host = "localhost"; // hoặc IP server MySQL
$dbname = "mydata";
$user = "root";
$pass = "";

$conn = new mysqli($host, $user, $pass, $dbname);
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Lỗi kết nối cơ sở dữ liệu!"]);
    exit();
}

// Nhận dữ liệu từ Android gửi lên
$username = $_POST['username'] ?? '';
$password = $_POST['password'] ?? '';
$role = $_POST['role'] ?? 'user';

// Kiểm tra dữ liệu
if (empty($username) || empty($password)) {
    echo json_encode(["success" => false, "message" => "Thiếu thông tin tài khoản!"]);
    exit();
}

// Kiểm tra username đã tồn tại chưa
$stmt = $conn->prepare("SELECT * FROM users WHERE username = ?");
$stmt->bind_param("s", $username);
$stmt->execute();
$result = $stmt->get_result();
if ($result->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Tên đăng nhập đã tồn tại!"]);
    exit();
}
$stmt->close();

// Mã hóa mật khẩu (tuỳ chọn)
$hashed_password = password_hash($password, PASSWORD_DEFAULT);

// Thêm tài khoản mới
$stmt = $conn->prepare("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $username, $hashed_password, $role);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Tạo tài khoản thành công!"]);
} else {
    echo json_encode(["success" => false, "message" => "Tạo tài khoản thất bại!"]);
}

$stmt->close();
$conn->close();
?>