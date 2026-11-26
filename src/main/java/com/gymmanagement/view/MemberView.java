package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map; //lưu dữ liệu dạng key-value
import javax.swing.*;
import javax.swing.border.EmptyBorder; //khoảng cách trống bên trong component
import javax.swing.table.DefaultTableModel;//quản lí dữ liệu hàng và cột của bảng JTable

/**
 * Giao diện Quản lý hội viên (Member Management)
 * Các trường: MemberID, FullName, Email, Phone, JoinDate, Address, BirthDate, Gender.
 * Sửa/xóa bằng biểu tượng ở cột Thao tác; nút làm mới nằm trên đầu bảng cạnh số hội viên.
 */
public class MemberView extends JPanel {
    //định nghĩa 1 hằng số đại diện cho vị trí index của cột Action bảng JTable
    //trong giao diện cột thứ 9 là cột có các thao tác, sửa, xoá
    public static final int ACTION_COLUMN = 8;
    //static thuộc về class, ko cần tạo đối tượng để sử dụng
    //final giá trị ko thể thay đổi sau khi được gán

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnAdd;

    public MemberView() {
        setLayout(new BorderLayout(0, 16));

        // thiết lập màu nền, 1 biến màu được định nghĩa trong class UITheme
        setBackground(UITheme.BG_MAIN);

        //hoặc có thể sử dụng setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24)); của javax.swing.*;
        setBorder(new EmptyBorder(24, 24, 24, 24));

        initUI();
    }

    private void initUI() {
        // Toolbar: tìm kiếm bên trái, nút thêm bên phải
        JPanel topPanel = UITheme.createCardPanel(); //tạo 1 panel được thiết kế sẳn, nền trắng viền xám

        //tạo 1 panel chứa các tp tiềm kiếm , sắp xếp chúng nằm ngang, bắt đầu từ trái.
        //flowLayout sắp xếp các component theo dòng, từ trái sang phải, ko đủ thì xuống dòng
        //FlowLayout có 3 tham số, alignment: căn các component theo phía nào, hgap: kc ngang giữa các componant, vgap: kc dọc ..
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);// panel ko vẽ nền riêng, cho phép phần nền của component phía sau có thể nhìn thấy qua panel
        //nếu true thì panel vẽ nền bằng màu được thiết lập.

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);// font SegoeUI, chữ in đậm, kích thước 16
        lblSearch.setForeground(UITheme.TEXT_DARK);//màu xanh dương đậm

        //Định dạng textField chuẩn
        //số 22 dùng để tính chiều rộng ưu tiên của ô, ko phải giới hạn kí tự
        //Font chữ: Segoe UI, cỡ 13, kiểu thường (Plain).
        //Màu chữ: TEXT_DARK – xanh đen đậm (#0F172A).
        //Màu con trỏ nhập: TEXT_DARK – xanh đen đậm (#0F172A).
        //Viền: BORDER – xám xanh nhạt (#E2E8F0), dày 1 pixel.
        //Padding bên trong: Trên/dưới 6 pixel, trái/phải 10 pixel.
        txtSearch = UITheme.createTextField(22);
        txtSearch.setToolTipText("Nhập mã hội viên hoặc họ tên cần tìm...");//khi di chuột lên ô tìm kiếm sẽ hiện dòng chú thích

        //tạo nút tìm kiếm
        // Nền: màu xanh dương PRIMARY (#1E3A8A)
        // Chữ: màu trắng.
        // Có hiệu ứng hover, pressed khi tương tác.
        // Bo góc nút, giao diện đồng nhất với các nút chính khác.
        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        //tạo 1 actionPanel chứa nhóm nút thao tác
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);//- Đặt panel trong suốt, không tô màu nền, để nền của panel phía dưới hiển thị xuyên qua.
        btnAdd = UITheme.createPrimaryButton("+ Thêm hội viên");//tạo nút thêm hội viên
        actionPanel.add(btnAdd);

        //arrangeToolbar() được viết sẵn trong UITheme để sắp xếp các thành phần trên thanh công cụ (toolbar)
        //JPanel toolbar(panel tổng), JPanel filters(phía trái west), JPanel actions(phải east)
        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số hội viên và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();//Tạo Card Panel với nền trắng và viền xám nhẹ
        tablePanel.setLayout(new BorderLayout(0, 10));

        //public TableHeader(String title, String refreshTooltip)
        //title: Tên tiêu đề khu vực bảng
        //refreshTooltip: Nội dung mô tả nút làm mới
        //Đầu bảng dùng chung: tiêu đề kèm số dòng đang hiện bên trái, nút làm mới dạng biểu tượng bên phải.
        tableHeader = new TableHeader("Danh sách hội viên", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã HV", "Họ và Tên", "Email", "Số Điện Thoại",
                "Ngày Tham Gia", "Địa Chỉ", "Ngày Sinh", "Giới Tính", "Thao tác"
        };
        // DefaultTableModel: class có sẵn của Java Swing, dùng để quản lý dữ liệu cho JTable (dữ liệu hàng, cột).
        // columns: mảng tên các cột đã khai báo trước đó, ví dụ Mã hội viên, Họ tên, Số điện thoại.
        // 0: số dòng dữ liệu ban đầu, nghĩa là lúc tạo model chưa có dòng dữ liệu nào.
        tableModel = new DefaultTableModel(columns, 0) {
            // Phương thức này kiểm tra một ô trong bảng có được phép chỉnh sửa trực tiếp hay không.
            // - row: vị trí hàng.
            // - column: vị trí cột.
            // - boolean: kiểu trả về chỉ có true hoặc false.
            // - return false: không cho phép chỉnh sửa ô.
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = createTable();
        // Email và Địa chỉ dài nên được nhiều chỗ hơn
        //nếu đủ ko gian thì độ rộng mong muốn sẽ được ưu tiên hiểU thị
        //Nếu chỉ dùng setPreferredWidth() mà không dùng setMinWidth(), 
        // khi bảng thiếu không gian, cột có thể bị thu nhỏ quá mức, khiến nội dung khó đọc.
        int[] widths = {66, 140, 160, 108, 104, 170, 100, 76};//độ rộng mong muốn
        int[] minWidths = {62, 110, 110, 104, 100, 100, 100, 72};//độ rộng tối thiểu
        //duyệt lần lượt các cột
        //getColumnModel:là hàm có sẳn trong JTable .Lấy đối tượng quản lý cấu trúc các cột của bảng
        //setPreferredWidth: Thiết lập độ rộng mong muốn cho cột
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
        }
        
        // Cột Thao tác dạng biểu tượng ở cuối bảng: xem / sửa / check-out / xóa / cấp tài khoản.
        // Các biểu tượng chia đều bề ngang ô; tập biểu tượng đổi theo quyền bằng {@link #setActions},
        // không còn biểu tượng nào thì cột bị ẩn. Biểu tượng có thể bị làm mờ theo từng dòng bằng {@link #setDisabledWhen}.
        //table: truyền đối tượng JTable vào để biết bảng nào được gắn thao tác.
        //Map.of() là phương thức có sẵn của Java, dùng để tạo một Map chứa các cặp Key – Value.
                actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.EDIT, "Chỉnh sửa hội viên",
                Action.DELETE, "Xóa hội viên"));
        //Action.EDIT chỉ là giá trị đại diện cho hành động chỉnh sửa, nó giống như một nhãn để chương trình nhận biết đang nói đến thao tác nào.
        //Action là 1 enum

        //register(Action.EDIT, Icons.edit(UITheme.PRIMARY_HOVER), Icons.edit(DISABLED));
        //Action.EDIT → xác định đây là hành động chỉnh sửa.
        //Icons.edit(UITheme.PRIMARY_HOVER) → tạo biểu tượng cây bút khi được kích hoạt.
        //Icons.edit(DISABLED) → tạo biểu tượng cây bút ở trạng thái bị làm mờ.
        
        
        //tạo thanh cuộn cho bảng
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());//bỏ đường viền của thanh cuộn
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    // Tạo bảng: new JTable(tableModel) – tạo JTable sử dụng tableModel làm nguồn dữ liệu.
    // Hiển thị tooltip: Khi rê chuột vào cột Thao tác thì hiện hướng dẫn thao tác; khi rê vào các ô dữ liệu khác thì hiện đầy đủ nội dung ô, kể cả chữ bị cắt.
    // Trang trí bảng: UITheme.styleTable(t) – áp dụng giao diện thống nhất của dự án GYM.
    // Khóa đổi thứ tự cột: Không cho người dùng kéo thả thay đổi vị trí các cột.
    private JTable createTable() {
        //sử dụng tableModel làm nguồn dữ liệu
        JTable t = new JTable(tableModel) {
            // cửa sổ hẹp thì hiện thanh cuộn ngang để không bị mất cột
            @Override
            public boolean getScrollableTracksViewportWidth() { return UITheme.tracksViewportWidth(this); }
            @Override
            //getToolTipText: lấy nội dung tooltip (chữ hiện khi rê chuột).
            public String getToolTipText(MouseEvent e) {
                //getPoint: ấy tọa độ vị trí con chuột tại thời điểm xảy ra sự kiện.
                //isAt() là phương thức được lập trình để kiểm tra vị trí chuột có nằm tại vùng của actionColumn hay không.
                //tooltipAt: là phương thức trong ActionColumn, Nó chỉ hiển thị lời giải thích bằng chữ, 
                //            không thực hiện chức năng Sửa, Xóa hay các thao tác khác.
                if (actionColumn.isAt(e.getPoint())) return actionColumn.tooltipAt(e.getPoint());

                //dùng để xác định ô mà chuột đang rê vào và lấy dữ liệu của ô đó.
                int row = rowAtPoint(e.getPoint());//xác định chuột đang nằm trên hàng nào. hàm trong JTable
                int col = columnAtPoint(e.getPoint());//xác định chuột đang nằm trên cột nào.
                if (row < 0 || col < 0) return null;

                // Ô bị cắt chữ vẫn xem được đầy đủ khi rê chuột
                //Đây là dòng lấy giá trị dữ liệu của ô tại hàng row, cột col trong JTable.
                //getValueAt(row, col): phương thức lấy giá trị tại ô có hàng row và cột col.
                Object value = getValueAt(row, col);

                            // if (value == null || value.toString().isEmpty()) {
                            //     return null;
                            // } else {
                            //     return value.toString();
                            // }
                return value == null || value.toString().isEmpty() ? null : value.toString();
            }
        };
        UITheme.styleTable(t);
        //getTableHeader: Lấy phần tiêu đề cột của JTable. phương thức sẳn trong JTable
        //setReorderingAllowed(false): Không cho người dùng kéo thả để đổi thứ tự các cột,Phương thức của đối tượng JTableHeader(JTableHeader là phần tiêu đề nằm phía trên các cột của JTable.)
        t.getTableHeader().setReorderingAllowed(false); // cột Thao tác phải luôn nằm cuối,
        return t;
    }

    /** Tài khoản không có quyền quản lý: ẩn nút thêm và cột Thao tác. */
    public void setManageable(boolean manageable) {

        //thiết lập nút có hiển thị hay không.
        //manageable = true → nút Thêm được hiển thị.
        //manageable = false → nút Thêm bị ẩn.
        btnAdd.setVisible(manageable);
        //manageable == true: Cột Thao tác hiển thị biểu tượng Sửa (EDIT) và Xóa (DELETE).
        //manageable == false: setActions() không truyền hành động nào → ẩn cột Thao tác.
        if (manageable) actionColumn.setActions(Action.EDIT, Action.DELETE);
        else actionColumn.setActions();
    }

    //Mục đích: cập nhật số lượng dữ liệu đang hiển thị trên phần tiêu đề của bảng.
    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    //Các thành phần giao diện thường được khai báo private để bảo vệ dữ liệu và tránh class khác truy cập trực tiếp.
    // VD: Controller không thể truy cập trực tiếp btnAdd nếu nó là private, nên View cung cấp getter:
    public JTable getTable() { return table; }//trả về bảng table đang được tạo trong class này. Controller có thể lấy bảng để kiểm tra dòng được chọn hoặc lấy dữ liệu trên bảng.
    public DefaultTableModel getTableModel() { return tableModel; }//Trả về tableModel, mô hình chứa dữ liệu của bảng.Controller có thể thêm, xóa, cập nhật các dòng dữ liệu thông qua model.
    public ActionColumn getActionColumn() { return actionColumn; }//Trả về đối tượng ActionColumn. - Cho phép class khác truy cập cấu hình cột chứa các icon chức năng như EDIT, DELETE.
    public JTextField getTxtSearch() { return txtSearch; }//Trả về ô nhập tìm kiếm txtSearch.Controller có thể lấy nội dung người dùng nhập để xử lý tìm kiếm.
    public JButton getBtnSearch() { return btnSearch; }//Trả về nút Search.,Controller có thể gắn sự kiện click cho nút tìm kiếm.
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }//Trả về nút Refresh.tableHeader.getBtnRefresh() gọi getter của class tableHeader để lấy nút Refresh từ class đó, rồi trả nút này ra ngoài.
    public JButton getBtnAdd() { return btnAdd; }//Trả về nút Add.Controller có thể gắn sự kiện thêm mới cho nút này.
}
