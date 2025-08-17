package gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import jlib.core.IDataManager;
import jlib.core.ISoundManager;
import jlib.core.ISystemManager;
import jlib.core.IWindowManager;
import jlib.core.JMPCoreAccessor;
import jlib.plugin.IPlugin;
import layout.LayoutConfig;
import layout.LayoutManager;
import plg.PropertiesNode;
import plg.PropertiesNode.PropertiesNodeType;
import plg.SystemProperties;

public class RendererConfigDialog extends JDialog implements ActionListener {

    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTable rendererTable;
    private JTable designTable;

    private DefaultTableModel rendererModel;
    private DefaultTableModel designModel;
    private JTabbedPane tabbedPane;

    private IPlugin targetPlg;

    // ユーザー非公開キー
    private List<String> ignoreKeysSystem = Arrays.asList(SystemProperties.SYSP_LAYOUT, SystemProperties.SYSP_RENDERER_KEYWIDTH);
    private List<String> ignoreKeysDesign = Arrays.asList(LayoutConfig.LC_CURSOR_POS, LayoutConfig.LC_NOTES_COLOR_ASIGN, LayoutConfig.LC_PB_COLOR,
            LayoutConfig.LC_PB_VISIBLE);

    private Map<Integer, JComboBox<String>> comboBoxMapSys = new HashMap<>();
    private Map<Integer, JComboBox<String>> comboBoxMapLc = new HashMap<>();
    private JLabel lblSelectedSysth;

    // 行によってエディタを切り替えるクラス
    class RowSpecificComboBoxEditor extends AbstractCellEditor implements TableCellEditor {
        private final JTextField textField;
        private Component currentEditor;
        private boolean isCombobox = false;
        private int lastSelectedRow = -1;
        private Map<Integer, JComboBox<String>> comboBoxMap;
        private DefaultTableModel model;

        public RowSpecificComboBoxEditor(Map<Integer, JComboBox<String>> comboBoxMap, DefaultTableModel model) {
            this.textField = new JTextField();
            this.comboBoxMap = comboBoxMap;
            this.model = model;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object getCellEditorValue() {
            Object val = null;
            if (isCombobox) {
                val = ((JComboBox<String>) currentEditor).getSelectedItem();
            }
            else {
                val = textField.getText();
            }
            this.model.setValueAt(val, lastSelectedRow, 1);
            // System.out.println(val);
            return val;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (this.comboBoxMap.containsKey(row)) {
                this.comboBoxMap.get(row).setSelectedItem(value);
                currentEditor = this.comboBoxMap.get(row);
                isCombobox = true;
            }
            else {
                textField.setText(value != null ? value.toString() : "");
                currentEditor = textField;
                isCombobox = false;
            }
            lastSelectedRow = row;
            return currentEditor;
        }
    }

    // 行によってエディタを切り替えるクラス
    class SystRowSpecificComboBoxEditor extends RowSpecificComboBoxEditor {
        public SystRowSpecificComboBoxEditor() {
            super(comboBoxMapSys, rendererModel);
        }
    }

    // 行によってエディタを切り替えるクラス
    class LcRowSpecificComboBoxEditor extends RowSpecificComboBoxEditor {
        public LcRowSpecificComboBoxEditor() {
            super(comboBoxMapLc, designModel);
        }
    }

    /**
     * Create the dialog.
     */
    public RendererConfigDialog(IPlugin plg) {
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setTitle("Renderer Settings");
        this.targetPlg = plg;
        setModal(true);
        setBounds(100, 100, 609, 414);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            tabbedPane = new JTabbedPane(JTabbedPane.TOP);
            contentPanel.add(tabbedPane);
            {
                JPanel audioPanel = new JPanel();
                tabbedPane.addTab("Audio", null, audioPanel, null);
                audioPanel.setLayout(null);

                JButton btnSelectSynthButton = new JButton("Select Synthesizer");
                btnSelectSynthButton.setActionCommand("SELECT_SYNTH");
                btnSelectSynthButton.addActionListener(this);
                btnSelectSynthButton.setBounds(28, 38, 204, 26);
                audioPanel.add(btnSelectSynthButton);

                lblSelectedSysth = new JLabel("Nothing");
                lblSelectedSysth.setFont(new Font("MS UI Gothic", Font.PLAIN, 18));
                lblSelectedSysth.setBounds(66, 74, 500, 26);
                audioPanel.add(lblSelectedSysth);
            }
            {
                JPanel rendererPanel = new JPanel();
                tabbedPane.addTab("Renderer", null, rendererPanel, null);
                rendererPanel.setLayout(new BorderLayout(0, 0));
                {
                    // テーブルのデータとカラム名
                    String[] columnNames = { "Config", "Value" };
                    Object[][] data = { { 1, "A" }, { 2, "B" }, { 3, "C" } };

                    // モデル作成
                    rendererModel = new DefaultTableModel(data, columnNames);

                    rendererTable = new JTable(rendererModel);
                    JScrollPane scrollPane = new JScrollPane(rendererTable);
                    rendererPanel.add(scrollPane, BorderLayout.CENTER);
                }
            }
            {
                JPanel layout1Panel = new JPanel();
                tabbedPane.addTab("Layout1", null, layout1Panel, null);
                layout1Panel.setLayout(null);

                JButton btnLoadLayoutButton = new JButton("Load Layout Config");
                btnLoadLayoutButton.setActionCommand("LOAD_LAYOUT");
                btnLoadLayoutButton.addActionListener(this);
                btnLoadLayoutButton.setBounds(28, 38, 204, 26);
                layout1Panel.add(btnLoadLayoutButton);
                {
                    JButton btnDefaultButton = new JButton("Default Layout Config");
                    btnDefaultButton.setBounds(28, 81, 204, 26);
                    btnDefaultButton.setActionCommand("DEF_LAYOUT");
                    btnDefaultButton.addActionListener(this);
                    layout1Panel.add(btnDefaultButton);
                }
            }
            {
                JPanel Layout2Panel = new JPanel();
                tabbedPane.addTab("Layout2", null, Layout2Panel, null);
                Layout2Panel.setLayout(new BorderLayout(0, 0));
                {
                    // テーブルのデータとカラム名
                    String[] columnNames = { "Config", "Value" };
                    Object[][] data = { { 1, "A" }, { 2, "B" }, { 3, "C" } };

                    // モデル作成
                    designModel = new DefaultTableModel(data, columnNames);

                    designTable = new JTable(designModel);
                    JScrollPane scrollPane = new JScrollPane(designTable);
                    Layout2Panel.add(scrollPane);
                }
            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Launch Renderer");
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
                okButton.addActionListener(this);
            }
        }
    }

    public void updateItem() {
        updateSystemItems();
        updateDesignItems();
    }

    public void updateSystemItems() {
        rendererModel.setRowCount(0);
        comboBoxMapSys.clear();

        int i = 0;
        for (PropertiesNode node : SystemProperties.getInstance().getNodes()) {
            if (ignoreKeysSystem.contains(node.getKey())) {
                continue;
            }

            String keyName = node.getKey();
            if (SystemProperties.SwapKeyName.containsKey(keyName)) {
                keyName = SystemProperties.SwapKeyName.get(keyName);
            }
            Object[] row = { keyName, node.getDataString() };
            rendererModel.addRow(row);

            if (node.getType() == PropertiesNodeType.ITEM) {
                if (node.getItems().isEmpty() == false) {
                    JComboBox<String> cb = new JComboBox<String>(node.getItemArray());
                    comboBoxMapSys.put(i, cb);
                }
            }
            else if (node.getType() == PropertiesNodeType.BOOLEAN) {
                String[] boolArray = new String[] { "false", "true" };
                JComboBox<String> cb = new JComboBox<String>(boolArray);
                comboBoxMapSys.put(i, cb);
            }
            i++;
        }
        rendererTable.setRowHeight(20);

        // カスタムエディタを2列目に設定
        TableColumn col = rendererTable.getColumnModel().getColumn(1);
        col.setCellEditor(new SystRowSpecificComboBoxEditor());
    }

    public void updateDesignItems() {
        int i = 0;
        designModel.setRowCount(0);
        for (PropertiesNode node : LayoutManager.getInstance().getNodes()) {
            if (ignoreKeysDesign.contains(node.getKey())) {
                continue;
            }

            String keyName = node.getKey();
            if (LayoutConfig.SwapKeyName.containsKey(keyName)) {
                keyName = LayoutConfig.SwapKeyName.get(keyName);
            }
            Object[] row = { keyName, node.getDataString() };
            designModel.addRow(row);

            if (node.getType() == PropertiesNodeType.ITEM) {
                if (node.getItems().isEmpty() == false) {
                    JComboBox<String> cb = new JComboBox<String>(node.getItemArray());
                    comboBoxMapLc.put(i, cb);
                }
            }
            else if (node.getType() == PropertiesNodeType.BOOLEAN) {
                String[] boolArray = new String[] { "false", "true" };
                JComboBox<String> cb = new JComboBox<String>(boolArray);
                comboBoxMapLc.put(i, cb);
            }
            i++;
        }
        designTable.setRowHeight(20);

        // カスタムエディタを2列目に設定
        TableColumn col = designTable.getColumnModel().getColumn(1);
        col.setCellEditor(new LcRowSpecificComboBoxEditor());
    }

    public void updateSelectedSynthLabel() {
        String name = JMPCoreAccessor.getDataManager().getConfigParam(IDataManager.CFG_KEY_MIDIOUT);
        if (name.isBlank()) {
            name = "Automatic selection synthesizer.";
        }
        else if (name.equals(ISoundManager.NULL_RECEIVER_NAME)) {
            name = "Don't choose a synthesizer.";
        }
        else if (name.equals(ISoundManager.RENDER_ONLY_RECEIVER_NAME)) {
            name = "Rendering only.";
        }
        lblSelectedSysth.setText(name);
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            updateItem();
            updateSelectedSynthLabel();
        }
        super.setVisible(b);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();

        switch (cmd) {
            case "LOAD_LAYOUT":
                JFileChooser chooser = new JFileChooser();
                chooser.setDialogTitle("Select layout config.");
                chooser.addChoosableFileFilter(new FileNameExtensionFilter("Layout config (*.layout)", "layout"));

                // ファイル選択ダイアログを表示
                Path folder = Paths.get(JMPCoreAccessor.getSystemManager().getSystemPath(ISystemManager.PATH_RES_DIR, targetPlg));
                chooser.setCurrentDirectory(new File(folder.toString())); // 初期フォルダ
                int result = chooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = chooser.getSelectedFile();
                    Path path = selectedFile.toPath(); // Path型に変換
                    try {
                        LayoutManager.getInstance().read(new File(path.toString()));
                        updateDesignItems();
                        tabbedPane.setSelectedIndex(3);
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                break;
            case "DEF_LAYOUT":
                LayoutManager.getInstance().initializeConfig();
                updateDesignItems();
                tabbedPane.setSelectedIndex(3);
                break;
            case "SELECT_SYNTH":
                JMPCoreAccessor.getWindowManager().getWindow(IWindowManager.WINDOW_NAME_MIDI_SETUP).showWindow();
                updateSelectedSynthLabel();
                break;
            case "OK":
                if (rendererTable.isEditing()) {
                    rendererTable.getCellEditor().stopCellEditing();
                }
                for (int i = 0; i < rendererModel.getRowCount(); i++) {
                    String key = (String) rendererModel.getValueAt(i, 0);
                    String param = (String) rendererModel.getValueAt(i, 1);
                    for (PropertiesNode node : SystemProperties.getInstance().getNodes()) {
                        if (node.getKey().equalsIgnoreCase(key) == true) {
                            node.setObject(param);
                            break;
                        }
                    }
                }
                if (designTable.isEditing()) {
                    designTable.getCellEditor().stopCellEditing();
                }
                for (int i = 0; i < designModel.getRowCount(); i++) {
                    String key = (String) designModel.getValueAt(i, 0);
                    String param = (String) designModel.getValueAt(i, 1);
                    for (PropertiesNode node : LayoutManager.getInstance().getNodes()) {
                        if (node.getKey().equalsIgnoreCase(key) == true) {
                            node.setObject(param);
                            break;
                        }
                    }
                }
                setVisible(false);
                break;
            case "Cancel":
                setVisible(false);
                break;
        }
    }
}
