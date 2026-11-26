package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.Set;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// Giao diện Quản lý nhân viên (Staff Management)
// Các trường: StaffID, FullName, Phone, Salary, Shift, HireDate, Position, Username, trạng thái mật khẩu.
// Cấp tài khoản / sửa / xóa bằng biểu tượng ở cột Thao tác; nút làm mới nằm trên đầu bảng cạnh số nhân viên.
// Chỉ Quản lý và Lễ tân được cấp tài khoản: dòng của chức vụ khác có chìa khóa mờ và cột tài khoản ghi "Không áp dụng".
// Mật khẩu được băm một chiều nên cột Mật khẩu chỉ hiện trạng thái, không hiện được mật khẩu thật.

public class StaffView extends JPanel {
    public static final int ACTION_COLUMN = 9;
    // Chữ ở cột Tên đăng nhập / Mật khẩu khi nhân viên chưa có tài khoản
    public static final String NO_ACCOUNT = "Chưa cấp";
    // Chữ ở cột Tên đăng nhập / Mật khẩu khi chức vụ không được cấp tài khoản (chỉ Quản lý, Lễ tân có)
    public static final String NOT_APPLICABLE = "Không áp dụng";

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;
    // Nhân viên đang đăng nhập: không được tự xóa nên biểu tượng xóa ở dòng này bị làm mờ
    private String currentStaffId;//biến lưu mã nhân viên, để biết nv nào đang đăng nhập

    // Nhân viên có chức vụ được đăng nhập: chỉ các dòng này bấm được biểu tượng chìa khóa
    //Set<String>:Tập hợp chứa các chuỗi, không có phần tử trùng lặp.
    //Set.of()	Khởi tạo một tập hợp rỗng, không có dữ liệu ban đầu.
    private Set<String> loginStaffIds = Set.of();

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnAdd;

    public StaffView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: tìm kiếm bên trái, nút thêm bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(22);
        txtSearch.setToolTipText("Mã nhân viên, Tên nhân viên, SĐT hoặc Tên đăng nhập...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAdd = UITheme.createPrimaryButton("+ Thêm nhân viên");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số nhân viên và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách nhân viên", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã NV", "Họ và Tên", "Số Điện Thoại", "Lương CB",
                "Ca Làm Việc", "Ngày Tuyển", "Chức Vụ", "Tên Đăng Nhập", "Mật Khẩu", "Thao tác"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel) {
             @Override
            public boolean getScrollableTracksViewportWidth() { return UITheme.tracksViewportWidth(this); }
            @Override
            public String getToolTipText(MouseEvent e) {
                if (actionColumn.isAt(e.getPoint())) return actionColumn.tooltipAt(e.getPoint());
                int row = rowAtPoint(e.getPoint());
                int col = columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) return null;
                // Ô bị cắt chữ vẫn xem được đầy đủ khi rê chuột
                Object value = getValueAt(row, col);
                return value == null ? null : value.toString();
            }
        };
        UITheme.styleTable(table);
        //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác phải luôn nằm cuối
        // Cột mã ngắn nhường chỗ cho Họ tên, Ca làm việc và Tên đăng nhập
        int[] widths = {66, 150, 106, 100, 140, 94, 106, 118, 108};
        int[] minWidths = {60, 100, 96, 88, 90, 90, 76, 84, 84};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(minWidths[i]);
        }
        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.ACCOUNT, "Cấp tài khoản / đặt lại mật khẩu",
                Action.EDIT, "Chỉnh sửa nhân viên",
                Action.DELETE, "Xóa nhân viên"));
        
        //setActions() là phương thức được xây dựng trong class ActionColumn, dùng để thiết lập những thao tác được hiển thị trong cột Thao tác.
        actionColumn.setActions(Action.ACCOUNT, Action.EDIT, Action.DELETE);

        //setDisabledWhen(điều kiện_vô_hiệu_hóa, thông_báo_lý_do);
        //(row, action) ->	Xác định điều kiện nào khiến thao tác bị vô hiệu hóa.Đây là biểu thức Lambda được truyền vào phương thức setDisabledWhen().
        //row đại diện cho dòng nhân viên mà ActionColumn đang kiểm tra.VD ktr dong 1, row đại diện cho dữ liệu của NV001
        // /action đại diện cho thao tác đang được kiểm tra trên dòng nhân viên đó.
        //Dấu -> trong Lambda có nghĩa là đưa các tham số ở bên trái vào phần biểu thức xử lý ở bên phải.

        //isCurrentUserRow(row):dùng để kiểm tra dòng nhân viên đang xét có phải là nhân viên đang đăng nhập vào hệ thống hay không.
        //Chỉ khi đang xét thao tác Xóa và nhân viên ở dòng đó chính là người đang đăng nhập thì biểu thức mới trả về true.
        //Mục đích của điều kiện thứ nhất: Ngăn người đang đăng nhập xóa chính tài khoản nhân viên của mình thông qua màn hình quản lý nhân viên.

        //canLoginRow(row): dùng để kiểm tra nhân viên ở dòng đang xét có được phép cấp tài khoản đăng nhập hay không
        //Điều kiện thứ hai – Không cho cấp tài khoản đối với chức vụ không được phép
        actionColumn.setDisabledWhen((row, action) ->
                        (action == Action.DELETE && isCurrentUserRow(row)) || (action == Action.ACCOUNT && !canLoginRow(row)),
                Map.of(Action.DELETE, "Không thể xóa tài khoản đang đăng nhập",
                        Action.ACCOUNT, "Chức vụ này không được cấp tài khoản (chỉ Quản lý và Lễ tân)"));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    //Mục đích: Xác định xem mã nhân viên đang đăng nhập có trùng với mã nhân viên tại dòng đang xét hay không.
    // row: chỉ số dòng trong tableModel
    //tableModel.getValueAt(row, 0):Lấy giá trị tại dòng và cột được chỉ định.
    //String.valueOf() là phương thức của Java dùng để chuyển một giá trị thành chuỗi String.(vì getValueAT trả về Object)
    //equalsIgnoreCase() là phương thức so sánh hai chuỗi, không phân biệt chữ hoa và chữ thường.
    public boolean isCurrentUserRow(int row) {
        return currentStaffId != null && currentStaffId.equalsIgnoreCase(String.valueOf(tableModel.getValueAt(row, 0)));
    }

    //Nó có nhiệm vụ nhận mã nhân viên đang đăng nhập, lưu mã đó vào currentStaffId và yêu cầu bảng nhân viên vẽ lại giao diện.
    public void setCurrentStaffId(String staffId) {
        this.currentStaffId = staffId;
        table.repaint();//Đây là dòng yêu cầu Swing vẽ lại giao diện của bảng table.
    }//Trước khi gọi phương thức:currentStaffId = null;
    //Bảng chưa biết nhân viên nào là người đang đăng nhập, vì vậy chưa thể áp dụng điều kiện làm mờ biểu tượng Xóa dựa trên mã nhân viên đó.
    //Sau khi gọi:staffView.setCurrentStaffId("NV001");đã cập nhật mã nhân viên đang đăng nhập thành "NV001".
    //-->yêu cầu bảng vẽ lại giao diện, giúp giao diện cập nhật theo trạng thái hiện tại.
    //Mục đích: Yêu cầu bảng cập nhật việc hiển thị các biểu tượng thao tác theo mã nhân viên đang đăng nhập vừa được thiết lập.

    //dùng để kiểm tra một dòng nhân viên trong bảng có tài khoản đăng nhập hay không.
    //loginStaffIds: tập hợp lưu mã nhân viên đã có tài khoản đăng nhập.
    //contains(): kiểm tra một phần tử có tồn tại trong tập hợp hay không.
    private boolean canLoginRow(int row) {
        return loginStaffIds.contains(String.valueOf(tableModel.getValueAt(row, 0)));
    }//Nếu mã nhân viên có trong loginStaffIds thì trả về true, nếu không có thì trả về false.

    //cập nhật tập hợp mã nhân viên đã có tài khoản đăng nhập và yêu cầu JTable vẽ lại giao diện để cập nhật trạng thái các biểu tượng thao tác.
    //Set<String> staffIds: tham số nhận vào một tập hợp các mã nhân viên có tài khoản đăng nhập.
    public void setLoginStaffIds(Set<String> staffIds) {
        this.loginStaffIds = Set.copyOf(staffIds);
        table.repaint();
    }
    //this.loginStaffIds = Set.copyOf(staffIds);: sao chép các mã nhân viên từ staffIds vào biến loginStaffIds của StaffView. Set.copyOf() tạo một tập hợp không thể thay đổi (immutable).
    //table.repaint();: yêu cầu JTable vẽ lại giao diện để cập nhật trạng thái hiển thị của các biểu tượng thao tác dựa trên tập hợp mã nhân viên mới.

    // Tài khoản không có quyền quản lý: ẩn nút thêm và cột Thao tác
    public void setManageable(boolean manageable) {
        btnAdd.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.ACCOUNT, Action.EDIT, Action.DELETE);
        else actionColumn.setActions();
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAdd() { return btnAdd; }
}
