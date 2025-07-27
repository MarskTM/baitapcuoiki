<?php
header("Content-Type: application/json");

// Nhận JSON từ client
$json = file_get_contents("php://input");
$data = json_decode($json, true);

// Ghi log lại để debug khi cần
file_put_contents("log_register.txt", $json . PHP_EOL, FILE_APPEND);

// Kiểm tra dữ liệu đầu vào
if (!$data || !isset($data['username']) || !isset($data['password'])) {
    echo json_encode(["success" => false, "message" => "Dữ liệu không hợp lệ"]);
    exit;
}

$creatorRole = $data["creator_role"] ?? "";
$username = trim($data["username"]);
$password = trim($data["password"]);
$role = trim($data["role"] ?? "user");

// Không cho tạo admin nếu không phải admin
if ($role === "admin" && $creatorRole !== "admin") {
    echo json_encode(["success" => false, "message" => "Chỉ admin mới được tạo tài khoản admin"]);
    exit;
}

// Kết nối MySQL
$conn = new mysqli("localhost", "root", "", "Mydata");
if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "Lỗi kết nối: " . $conn->connect_error]);
    exit;
}

// Kiểm tra trùng username
$checkStmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
$checkStmt->bind_param("s", $username);
$checkStmt->execute();
$checkStmt->store_result();
if ($checkStmt->num_rows > 0) {
    echo json_encode(["success" => false, "message" => "Username đã tồn tại"]);
    exit;
}

// Thêm user mới
$stmt = $conn->prepare("INSERT INTO users (username, password, role) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $username, $password, $role);

if ($stmt->execute()) {
    $newUserId = $stmt->insert_id;
    echo json_encode([
        "success" => true,
        "user" => [
            "id" => $newUserId,
            "username" => $username,
            "role" => $role
        ]
    ]);
} else {
    echo json_encode(["success" => false, "message" => "Lỗi insert: " . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
