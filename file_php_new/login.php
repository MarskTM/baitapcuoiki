<?php
// Thông tin kết nối MySQL
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "mydata";

// Kết nối MySQL
$conn = new mysqli($servername, $username, $password, $dbname);
if ($conn->connect_error) {
    die(json_encode(['error' => 'Kết nối thất bại: ' . $conn->connect_error]));
}

// Cho phép truy cập từ app
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");

// Đọc JSON từ body
$json = file_get_contents("php://input");
$data = json_decode($json);

if (!$data || !isset($data->username) || !isset($data->password)) {
    echo json_encode(['error' => 'Dữ liệu đầu vào không hợp lệ']);
    exit;
}

$inputUsername = $conn->real_escape_string($data->username);
$inputPassword = $data->password;

// Tìm user trong CSDL
$sql = "SELECT * FROM users WHERE username = '$inputUsername' LIMIT 1";
$result = $conn->query($sql);

if ($result && $result->num_rows > 0) {
    $user = $result->fetch_assoc();

    // So sánh mật khẩu: kiểm tra nếu là dạng hash hay không
    if (
        password_verify($inputPassword, $user['password']) ||  // với mật khẩu được băm
        $inputPassword === $user['password']                    // hoặc mật khẩu lưu dạng text
    ) {
        unset($user['password']); // Xoá mật khẩu trước khi trả về
        echo json_encode([
            'success' => true,
            'user' => $user
        ]);
    } else {
        echo json_encode(['error' => 'Sai mật khẩu']);
    }
} else {
    echo json_encode(['error' => 'Tài khoản không tồn tại']);
}

$conn->close();
?>
