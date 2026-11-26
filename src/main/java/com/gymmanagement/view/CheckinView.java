package com.gymmanagement.view;

import com.gymmanagement.view.ActionColumn.Action;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Giao diện Quản lý Check-in / Check-out
 * Các trường: CheckinID, MemberID, MemberName, CheckinTime, CheckoutTime, ReceptionistID.
 * Check-out và xóa bằng biểu tượng ở cột Thao tác; lượt chưa ra hiện "Đang tập" ở cột Giờ ra.
 */
public class CheckinView extends JPanel {
    public static final int CHECKOUT_COLUMN = 4;
    public static final int ACTION_COLUMN = 6;
    /** Chữ hiện ở cột Giờ ra khi hội viên chưa check-out. */
    public static final String STILL_INSIDE = "Đang tập";

    private JTable table;
    private DefaultTableModel tableModel;
    private ActionColumn actionColumn;
    private TableHeader tableHeader;

    private JTextField txtSearch;
    private JButton btnSearch;
    private JButton btnCheckin;

    public CheckinView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: tìm kiếm bên trái, nút check-in bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(22);
        txtSearch.setToolTipText("Mã check-in, Mã hội viên hoặc Tên hội viên...");

        btnSearch = UITheme.createPrimaryButton("Tìm kiếm");

        searchPanel.add(lblSearch);
        searchPanel.add(txtSearch);
        searchPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnCheckin = UITheme.createPrimaryButton("+ Check-in vào");
        actionPanel.add(btnCheckin);

        UITheme.arrangeToolbar(topPanel, searchPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số lượt, số người đang tập và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Lịch sử check-in", "Làm mới (xóa ô tìm kiếm và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {
                "Mã Check-in", "Mã Hội Viên", "Tên Hội Viên",
                "Giờ Vào", "Giờ Ra", "Lễ Tân Trực", "Thao tác"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel) {
            // cửa sổ hẹp thì hiện thanh cuộn ngang để không bị mất cột
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
                return value == null || value.toString().isEmpty() ? null : value.toString();
            }
        };
        UITheme.styleTable(table);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác phải luôn nằm cuối
        int[] widths = {100, 100, 180, 140, 140, 110};
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        styleCheckoutColumn();

        actionColumn = new ActionColumn(table, ACTION_COLUMN, Map.of(
                Action.CHECKOUT, "Check-out ngay (ghi giờ ra là bây giờ)",
                Action.DELETE, "Xóa lượt check-in"));
        actionColumn.setActions(Action.CHECKOUT, Action.DELETE);

        //Thiết lập điều kiện vô hiệu hóa nút thao tác trên bảng: nếu hành động đang xét là CHECKOUT và hội viên của dòng đó đã ra khỏi phòng gym thì 
        // vô hiệu hóa nút CHECKOUT, đồng thời cung cấp thông báo "Lượt này đã check-out" để giải thích lý do.
        //vô hiệu hóa (disable) nút CHECKOUT trên một dòng nếu hội viên đã check-out rồi
        //trong actionColumn: setDisabledWhen() là phương thức dùng để thiết lập điều kiện vô hiệu hóa một hành động (action) trên từng dòng của bảng.
        actionColumn.setDisabledWhen((row, action) -> action == Action.CHECKOUT && !isInside(row),
                Map.of(Action.CHECKOUT, "Lượt này đã check-out"));
        //isInside(row) → kiểm tra hội viên có đang ở trong phòng gym hay không.

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Ô Giờ ra của lượt chưa check-out hiện "Đang tập" chữ đậm màu xanh lá. */
    private void styleCheckoutColumn() {
        //Lấy cột CHECKOUT trong JTable lưu vào biến col
        TableColumn col = table.getColumnModel().getColumn(CHECKOUT_COLUMN);
        //col.getCellRenderer(): lấy bộ hiển thị hiện tại của cột.
        //DefaultTableCellRenderer: bộ hiển thị mặc định của Swing, thường dùng để hiển thị dữ liệu dạng chữ trong ô JTable.
        //base: lưu bộ hiển thị mặc định này để lát nữa tái sử dụng.
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) col.getCellRenderer();

        //setCellRenderer() dùng để thiết lập bộ hiển thị (renderer) cho các ô thuộc cột đó.
        //t: JTable đang yêu cầu hiển thị ô
        //value: Giá trị dữ liệu của ô
        //selected:	Ô có đang được chọn hay không
        //focus:	Ô có đang được focus hay không
        //row:	Vị trí hàng đang hiển thị
        //c:	Vị trí cột đang hiển thị
        col.setCellRenderer((t, value, selected, focus, row, c) -> {
            Component comp = base.getTableCellRendererComponent(t, value, selected, focus, row, c);
            if (STILL_INSIDE.equals(value)) {
                //value: giá trị của ô CHECKOUT mà JTable đang yêu cầu hiển thị.
                //UITheme.SUCCESS: màu xanh lá được định nghĩa sẵn trong class UITheme
                comp.setForeground(UITheme.SUCCESS);
                //deriveFont() là phương thức của lớp Font, cho phép tạo ra một font mới dựa trên font hiện có, nhưng thay đổi một thuộc tính nào đó.
                comp.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD));
            } else {
                //UITheme.FONT_REGULAR: font chữ thường của dự án, được khai báo:
                comp.setFont(UITheme.FONT_REGULAR);
            }
            return comp;
        });
    }

    // Tạo một phương thức nhận vào chỉ số dòng của bảng, lấy giá trị ở cột CHECKOUT của dòng đó rồi so sánh với trạng thái STILL_INSIDE. Nếu bằng nhau thì trả về true (hội viên đang tập), 
    // nếu không thì trả về false (hội viên đã check-out hoặc không ở trạng thái đang tập).
    public boolean isInside(int row) {
        return STILL_INSIDE.equals(tableModel.getValueAt(row, CHECKOUT_COLUMN));
    }

    /** Tài khoản không có quyền quản lý: ẩn nút check-in và cột Thao tác. */
    public void setManageable(boolean manageable) {
        btnCheckin.setVisible(manageable);
        if (manageable) actionColumn.setActions(Action.CHECKOUT, Action.DELETE);
        else actionColumn.setActions();
    }

    /** Số lượt đang hiện, kèm số người chưa check-out (nếu có). */
    public void setCount(int shown, int inside) {
        tableHeader.setCount(shown, inside > 0 ? inside + " đang tập" : null);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public ActionColumn getActionColumn() { return actionColumn; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnCheckin() { return btnCheckin; }
}
