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

// Ghi log input để debug (rất hữu ích)
$json = file_get_contents("php://input");
file_put_contents("log.txt", $json . "\n", FILE_APPEND);

// Giải mã JSON
$data = json_decode($json);
if (!$data) {
    echo json_encode(['error' => 'Dữ liệu không hợp lệ']);
    $conn->close();
    exit;
}

// Kiểm tra đủ tham số
if (isset($data->bpm) && isset($data->x) && isset($data->y) && isset($data->z) && isset($data->ts)) {
    $bpm = (int)$data->bpm;
    $x = (float)$data->x;
    $y = (float)$data->y;
    $z = (float)$data->z;
    $ts = (int)$data->ts;

    // Ghi vào cơ sở dữ liệu
    $sql = "INSERT INTO sensor_data (bpm, x, y, z, ts) VALUES ($bpm, $x, $y, $z, $ts)";
    if ($conn->query($sql) === TRUE) {
        echo json_encode(['status' => 'OK']);
    } else {
        echo json_encode(['error' => 'Lỗi SQL: ' . $conn->error]);
    }
} else {
    echo json_encode(['error' => 'Thiếu thông tin']);
}

$conn->close();
?>