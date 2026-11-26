/* =====================================================================
   View và Stored Procedure phục vụ truy vấn, thống kê.
   Ứng dụng Java chưa gọi tới; chạy tay một lần sau khi đã cài database
   (mở trong SSMS hoặc dùng sqlcmd với -f 65001). Chạy lại nhiều lần được.
   ===================================================================== */
USE GymManagementDB;
GO

-- Hội viên đang có gói còn hạn
CREATE OR ALTER VIEW vw_HoiVienConHan AS
SELECT m.MemberID, m.FullName, m.Phone, g.PackageName, s.EndDate
FROM Subscription s
JOIN Member m     ON m.MemberID  = s.MemberID
JOIN GymPackage g ON g.PackageID = s.PackageID
WHERE s.Status = N'Đang hoạt động'
  AND s.EndDate >= CAST(GETDATE() AS DATE);
GO

-- Doanh thu theo từng tháng
CREATE OR ALTER VIEW vw_DoanhThuThang AS
SELECT YEAR(PaymentDate) AS Nam, MONTH(PaymentDate) AS Thang,
       COUNT(*) AS SoHoaDon, SUM(Amount) AS DoanhThu
FROM Payment
GROUP BY YEAR(PaymentDate), MONTH(PaymentDate);
GO

-- Các gói một hội viên đã đăng ký, số tiền đã trả và còn nợ
CREATE OR ALTER PROCEDURE sp_LichSuHoiVien
    @MemberID VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT s.SubscriptionID, g.PackageName, s.StartDate, s.EndDate, s.Price,
           COALESCE(SUM(p.Amount), 0) AS DaTra,
           CASE WHEN s.Price > COALESCE(SUM(p.Amount), 0)
                THEN s.Price - COALESCE(SUM(p.Amount), 0) ELSE 0 END AS ConNo
    FROM Subscription s
    JOIN GymPackage g   ON g.PackageID = s.PackageID
    LEFT JOIN Payment p ON p.SubscriptionID = s.SubscriptionID
    WHERE s.MemberID = @MemberID
    GROUP BY s.SubscriptionID, g.PackageName, s.StartDate, s.EndDate, s.Price
    ORDER BY s.StartDate;
END
GO
