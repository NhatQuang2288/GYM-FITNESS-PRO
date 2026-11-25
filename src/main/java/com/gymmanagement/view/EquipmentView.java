package com.gymmanagement.view;

import com.gymmanagement.model.Equipment;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Giao diện Quản lý thiết bị (Equipment Management)
 * Các trường: EquipmentID, EquipmentName, Value, PurchaseDate, Status.
 * Trạng thái đổi ngay trong bảng bằng combobox; sửa/xóa bằng biểu tượng ở cột Thao tác.
 */
public class EquipmentView extends JPanel {
    public static final int STATUS_COLUMN = 4;
    public static final int ACTION_COLUMN = 5;
    private static final String FILTER_ALL = "Tất cả";

    private JTable table;
    private DefaultTableModel tableModel;
    private TableColumn actionColumn;
    /** Có quyền quản lý thì mới đổi được trạng thái và thấy cột Thao tác. */
    private boolean manageable = true;

    private JTextField txtSearch;
    private JComboBox<String> cbFilterStatus;
    private JButton btnSearch;
    private JButton btnAdd;
    private TableHeader tableHeader;

    public EquipmentView() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG_MAIN);
        setBorder(new EmptyBorder(24, 24, 24, 24));
        initUI();
    }

    private void initUI() {
        // Toolbar: bộ lọc bên trái, nút thêm bên phải
        JPanel topPanel = UITheme.createCardPanel();

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(UITheme.FONT_BOLD);
        lblSearch.setForeground(UITheme.TEXT_DARK);

        txtSearch = UITheme.createTextField(11); // vừa đủ để thanh công cụ nằm trên một hàng ở cửa sổ mặc định
        txtSearch.setToolTipText("Mã thiết bị hoặc Tên máy...");

        JLabel lblStatus = new JLabel("Trạng thái:");
        lblStatus.setFont(UITheme.FONT_BOLD);
        lblStatus.setForeground(UITheme.TEXT_DARK);

        //class ArrayList đã thực hiện các quy định của interface List. Vì vậy, một biến có kiểu List có thể tham chiếu đến đối tượng ArrayList
        //ArrayList<String> filterItems = new ArrayList<>(); đều được
        List<String> filterItems = new ArrayList<>();
        filterItems.add(FILTER_ALL);// thêm Tất cả
        filterItems.addAll(Equipment.STATUSES);//thêm các lựa chọn từ file dao

        //toArray() : chuyển danh sách thành mảng
        //new String[0] : tạo một mảng String có kích thước ban đầu là 0, dùng làm tham số để xác định kiểu mảng cần trả về.
        cbFilterStatus = new JComboBox<>(filterItems.toArray(new String[0]));
        cbFilterStatus.setFont(UITheme.FONT_REGULAR);
        cbFilterStatus.setBackground(Color.WHITE);

        btnSearch = UITheme.createPrimaryButton("Lọc / Tìm");

        filterPanel.add(lblSearch);
        filterPanel.add(txtSearch);
        filterPanel.add(lblStatus);
        filterPanel.add(cbFilterStatus);
        filterPanel.add(btnSearch);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        btnAdd = UITheme.createPrimaryButton("+ Thêm thiết bị");
        actionPanel.add(btnAdd);

        UITheme.arrangeToolbar(topPanel, filterPanel, actionPanel);

        // Bảng dữ liệu, phía trên có số thiết bị và nút làm mới
        JPanel tablePanel = UITheme.createCardPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tableHeader = new TableHeader("Danh sách thiết bị", "Làm mới (xóa bộ lọc và tải lại)");
        tablePanel.add(tableHeader, BorderLayout.NORTH);

        String[] columns = {"Mã Thiết Bị", "Tên Thiết Bị", "Giá Trị (VNĐ)", "Ngày Mua", "Trạng Thái Hoạt Động", "Thao tác"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            //Đây là phương thức kiểm tra xem một ô trong bảng có được phép chỉnh sửa hay không.
            //phương thức này trả về true chỉ khi tài khoản có quyền quản lý (manageable = true) và ô đang xét thuộc cột 
            // trạng thái (column = 4). Các ô còn lại đều trả về false.
            public boolean isCellEditable(int row, int column) {
                return manageable && column == STATUS_COLUMN;
            }
        };

        table = new JTable(tableModel) {
            // cửa sổ hẹp thì hiện thanh cuộn ngang để không bị mất cột
            @Override
            public boolean getScrollableTracksViewportWidth() { return UITheme.tracksViewportWidth(this); }

            @Override
            public String getToolTipText(MouseEvent e) {
                //e.getPoint(): Lấy tọa độ vị trí con chuột tại thời điểm xảy ra sự kiện, trả về một đối tượng Point chứa tọa độ x và y.
                //columnAtPoint() là phương thức của JTable, nhận tọa độ Point và trả về chỉ số cột mà tọa độ đó đang nằm bên trong.
                int col = columnAtPoint(e.getPoint());
                //Phương thức rowAtPoint() của JTable xác định xem tọa độ chuột đó đang nằm ở hàng (row) nào trong bảng.
                //Kiểm tra xem chuột có đang nằm trên 1 hàng dữ liệu của JLable không
                //Nếu không nằm trên hàng nào thì kết thúc phương thức getToolTipText() ngay bằng return null
                if (rowAtPoint(e.getPoint()) < 0) return null;
                //isEditHalf(e.getPoint()) trả về kiểu boolean: true → chuột nằm ở vùng chỉnh sửa, false → chuột nằm ở vùng xóa.
                //điều_kiện ? giá_trị_nếu_true : giá_trị_nếu_false;
                if (col == ACTION_COLUMN) return isEditHalf(e.getPoint()) ? "Chỉnh sửa thiết bị" : "Xóa thiết bị";
                if (col == STATUS_COLUMN && manageable) return "Bấm để đổi trạng thái";
                return null;
            }
        };
        UITheme.styleTable(table);
        //Dòng này giúp ô đang chỉnh sửa trong JTable nhận thao tác bàn phím thay vì để chính JTable xử lý bàn phím.
        table.setSurrendersFocusOnKeystroke(true);
        table.getTableHeader().setReorderingAllowed(false); // cột Thao tác và Trạng thái phải giữ đúng vị trí
        styleStatusColumn();
        styleActionColumn();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
    }

    /** Ô trạng thái: chữ màu theo mức độ, có mũi tên ▾ khi đổi được; bấm một lần là mở combobox. */
    private void styleStatusColumn() {
        TableColumn col = table.getColumnModel().getColumn(STATUS_COLUMN);
        //Renderer này quy định cách hiển thị nội dung từng ô, ví dụ font chữ, màu chữ, căn lề...
        //col.getCellRenderer() lấy ra đối tượng chịu trách nhiệm hiển thị (vẽ) nội dung các ô thuộc cột col.
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) col.getCellRenderer();
        //setCellRenderer() dùng để thiết lập bộ hiển thị (Renderer) cho cột col.
        col.setCellRenderer((t, value, selected, focus, row, c) -> {
            //base.getTableCellRendererComponent: dùng để lấy Component đã được chuẩn bị để hiển thị ô hiện tại, dựa trên bảng, giá trị, 
            // trạng thái chọn, focus và vị trí hàng/cột.
            JLabel l = (JLabel) base.getTableCellRendererComponent(t, value, selected, focus, row, c);

            //Nếu manageable == true → lấy value cộng thêm "   ▾".
            //Nếu manageable == false → lấy value chuyển thành String bằng String.valueOf(value).
            l.setText(manageable ? value + "   ▾" : String.valueOf(value));
            l.setForeground(statusColor(String.valueOf(value)));
            l.setFont(UITheme.FONT_REGULAR.deriveFont(Font.BOLD));
            return l;
        });

        JComboBox<String> editor = new JComboBox<>(Equipment.STATUSES.toArray(new String[0]));
        editor.setFont(UITheme.FONT_REGULAR);
        editor.setBackground(Color.WHITE);

        // DefaultCellEditor là lớp có sẵn trong Java Swing, giúp một ô trong JTable có thể được chỉnh sửa thông qua một component, chẳng hạn:
        // - JTextField → chỉnh sửa bằng cách nhập văn bản.
        // - JCheckBox → chỉnh sửa bằng cách tích / bỏ tích.
        // - JComboBox → chỉnh sửa bằng cách chọn một giá trị từ danh sách
        DefaultCellEditor cellEditor = new DefaultCellEditor(editor);
        //setClickCountToStart(int count) là phương thức của DefaultCellEditor, dùng để quy định số lần nhấn chuột cần thiết để bắt đầu chỉnh sửa ô.
        cellEditor.setClickCountToStart(1);//1 → nhấn chuột 1 lần để bắt đầu chỉnh sửa.
        //gắn bộ chỉnh sửa ô cellEditor vào cột col của JTable.
        col.setCellEditor(cellEditor);
        col.setPreferredWidth(200);
    }
    //xác định màu chữ tương ứng với trạng thái của thiết bị
    private static Color statusColor(String status) {
        if (Equipment.STATUS_GOOD.equals(status)) return UITheme.SUCCESS;
        if (Equipment.STATUS_NEEDS_PARTS.equals(status)) return UITheme.DANGER;
        if (Equipment.STATUS_MAINTENANCE_DUE.equals(status)) return UITheme.WARNING;
        return UITheme.TEXT_DARK;
    }

    /** Cột Thao tác: bút chì (nửa trái) để sửa, thùng rác (nửa phải) để xóa. */
    private void styleActionColumn() {
        actionColumn = table.getColumnModel().getColumn(ACTION_COLUMN);
        //dùng để hiển thị nội dung của từng ô (cell) trong JTable
        DefaultTableCellRenderer base = (DefaultTableCellRenderer) actionColumn.getCellRenderer();
        JLabel editIcon = new JLabel(Icons.edit(UITheme.PRIMARY_HOVER));
        JLabel deleteIcon = new JLabel(Icons.trash(UITheme.DANGER));
        JPanel cell = new JPanel(new GridLayout(1, 2));
        //căn icon nằm giữa theo chiều ngang bên trong JLabel.
        editIcon.setHorizontalAlignment(SwingConstants.CENTER);
        deleteIcon.setHorizontalAlignment(SwingConstants.CENTER);
        cell.add(editIcon);
        cell.add(deleteIcon);
        TableCellRenderer renderer = (t, value, selected, focus, row, c) -> {
            // Lấy màu nền xen kẽ / màu chọn từ renderer chung để cột này đồng bộ với các cột khác
            Component bg = base.getTableCellRendererComponent(t, "", selected, false, row, c);
            cell.setBackground(bg.getBackground());
            return cell;
        };
        actionColumn.setCellRenderer(renderer);
        actionColumn.setPreferredWidth(90);
        actionColumn.setMinWidth(90);
        actionColumn.setMaxWidth(90);
        actionColumn.setResizable(false);

        //Nếu di chuyển chuột đến ô có thao tác (ví dụ sửa, xóa) hoặc ô trạng thái có thể quản lý → con trỏ thành hình bàn tay.
        //addMouseMotionListener(...): đăng ký bộ lắng nghe sự kiện di chuyển chuột trên bảng.
        //new MouseAdapter(): tạo một đối tượng MouseAdapter để xử lý sự kiện chuột mà không cần triển khai toàn bộ các phương thức của MouseListener và MouseMotionListener.
        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                //table.columnAtPoint(...)	Xác định vị trí chuột đang nằm trong cột nào của JTable.
                int col = table.columnAtPoint(e.getPoint());
                //Dòng này tạo biến clickable kiểu boolean để kiểm tra xem chuột có đang nằm trên một hàng dữ liệu hợp lệ (table.rowAtPoint(e.getPoint()) >= 0) và 
                // đồng thời nằm ở cột thao tác (col == ACTION_COLUMN) hoặc cột trạng thái khi được phép quản lý (col == STATUS_COLUMN && manageable) hay không.
                //Nếu thỏa mãn cả hai phần điều kiện thì clickable = true, ngược lại clickable = false. Biến này sau đó được dùng để quyết định đổi con trỏ chuột thành hình bàn tay hay con trỏ mặc định.
                boolean clickable = table.rowAtPoint(e.getPoint()) >= 0
                        && (col == ACTION_COLUMN || (col == STATUS_COLUMN && manageable));

                //Dòng này thiết lập hình dạng con trỏ chuột cho JTable bằng setCursor().
                //Cursor.getPredefinedCursor() lấy kiểu con trỏ có sẵn của Java, trong đó toán tử 3 ngôi clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR sẽ chọn con trỏ hình bàn tay (HAND_CURSOR) nếu clickable == true
                //chọn con trỏ mặc định (DEFAULT_CURSOR). Nhờ đó, khi rê chuột vào vị trí có thể tương tác, con trỏ sẽ đổi thành hình bàn tay để báo hiệu cho người dùng.
                table.setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }
        });
    }

    /** Điểm bấm trong cột Thao tác rơi vào nửa trái (biểu tượng sửa) hay không. */
    //kiểm tra vị trí chuột p có nằm ở nửa bên trái của ô JTable mà chuột đang đứng hay không
    public boolean isEditHalf(Point p) {
        //table.getCellRect(row, column, false): lấy hình chữ nhật Rectangle đại diện cho ô tại hàng và cột vừa xác định, với false nghĩa là lấy vùng ô không tính phần khoảng cách mở rộng cho vùng spacing.
        //Rectangle cell: lưu tọa độ và kích thước của ô đó, gồm x, y, width, height.
        Rectangle cell = table.getCellRect(table.rowAtPoint(p), table.columnAtPoint(p), false);
        return p.x < cell.x + cell.width / 2;
        //p.x < cell.x + cell.width / 2: so sánh tọa độ x của chuột với điểm giữa ô; nếu chuột nằm bên trái điểm giữa thì trả về true, còn nếu nằm bên phải hoặc đúng giữa thì trả về false.
    }

    /** Tài khoản chỉ xem: ẩn cột Thao tác, ô trạng thái chỉ hiện chữ. */
    public void setManageable(boolean manageable) {
        this.manageable = manageable;
        boolean shown = table.getColumnModel().getColumnCount() == tableModel.getColumnCount();
        if (!manageable && shown) table.getColumnModel().removeColumn(actionColumn);
        if (manageable && !shown) table.getColumnModel().addColumn(actionColumn);
        table.repaint();
    }

    public void setCount(int shown) {
        tableHeader.setCount(shown);
    }

    public JTable getTable() { return table; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCbFilterStatus() { return cbFilterStatus; }
    public JButton getBtnSearch() { return btnSearch; }
    public JButton getBtnRefresh() { return tableHeader.getBtnRefresh(); }
    public JButton getBtnAdd() { return btnAdd; }
}
