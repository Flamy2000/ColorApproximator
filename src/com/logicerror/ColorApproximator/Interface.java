package com.logicerror.ColorApproximator;

import com.formdev.flatlaf.intellijthemes.materialthemeuilite.FlatGitHubDarkContrastIJTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

public class Interface extends JFrame {
    private JPanel contentPane;
    private JTabbedPane tabbedPane;
    private JButton selectImageButton;
    private JButton openOutputButton;
    private JButton runButton;
    private JButton openInputButton;
    private JButton downloadButton;
    private JPanel palettePane;
    private JScrollPane paletteScroll;
    private JProgressBar runProgressBar;
    ColorApproximation ca;

    private HashMap<JCheckBox, File> paletteRef = new HashMap<>();



    public Interface(JFrame parent){
        super("Color Approximator");

//        setTitle("Color Approximator");
        setContentPane(contentPane);
        setMinimumSize(new Dimension(400, 350));
        setSize(new Dimension(600, 350));
//        setModal(true);
        setLocationRelativeTo(parent);
        ca = new ColorApproximation();
        ca.setCallingInterface(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        createColorCheckBoxes();

        selectImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectImageButton();
            }
        });

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });

        openOutputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showOutput();
            }
        });

        openInputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showInput();
            }
        });

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                download();
            }
        });

        selectImageButton.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(
                                    DataFlavor.javaFileListFlavor);
                    for (File file : droppedFiles) {
                        String[] valid_types = {"png", "jpg", "jpeg", "gif", "bmp", "webmp"};
                        boolean valid = false;
                        for (String type : valid_types){
                            if (file.getName().endsWith("." + type) || file.getName().endsWith("." + type.toUpperCase())){
                                valid = true;
                            }
                        }
                        if (valid){
                            selectImage(file);
                            break;
                        }

                    }
                    evt.dropComplete(true);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Must be called last
        setVisible(true);
    }

    private void selectImageButton() {
        // JFileChooser points to user's default directory
        JFileChooser j = new JFileChooser(System.getProperty("user.dir") + "\\SampleImages");
        // FileSystemView.getFileSystemView().getHomeDirectory()
        //

        // Open the select dialog
        int r = j.showOpenDialog(null);
        if (r == JFileChooser.APPROVE_OPTION)
        {
            selectImage(j.getSelectedFile());
        }
    }

    private void selectImage(File file){
        try {
            ca.setImage(file);
//                ca.show();
            runButton.setEnabled(true);
            openOutputButton.setEnabled(false);
            openInputButton.setText("Show " + file.getName());
            openInputButton.setEnabled(true);
            downloadButton.setEnabled(false);
            runProgressBar.setValue(0);
            ca.resetPercentComplete();
        }catch (Picture.InvalidImageTypeException e){
            JOptionPane.showMessageDialog(contentPane, "Invalid image type",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void run(){
        // Get and combine color palettes
        Color[] palette = new Color[0];
        for (JCheckBox checkBox : paletteRef.keySet()) {
            try {
                if (checkBox.isSelected()) {
                    palette = ColorApproximation.combineColorPalettes(palette, ColorApproximation.readColorFile(paletteRef.get(checkBox)));
                }
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
        if (palette.length == 0){
            JOptionPane.showMessageDialog(contentPane, "Please select at least one color palette",
                    "Error", JOptionPane.WARNING_MESSAGE);
            tabbedPane.setSelectedIndex(1);
            return;
        }
        runButton.setEnabled(false);
        openOutputButton.setEnabled(false);
        downloadButton.setEnabled(false);

        ca.setPalette(palette);
        ca = new ColorApproximation(ca);
        ca.start();

        ProgressWorker progressWorker = new ProgressWorker();
        progressWorker.execute();

    }

    class ProgressWorker extends SwingWorker<Integer, Integer>
    {
        protected Integer doInBackground() throws Exception
        {
            // Do a time-consuming task.
            while (ca.isAlive()) {
                runProgressBar.setValue((int)Math.round(ca.getPercentComplete()*100));
            }
            return 1;
        }

//        protected void done()
//        {
//            try
//            {
//                JOptionPane.showMessageDialog(f, get());
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
    }

    void runCompleted(){
        openOutputButton.setEnabled(true);
        downloadButton.setEnabled(true);
        runButton.setEnabled(true);
    }

    private void showOutput(){
        ca.showOutput();
    }

    private void showInput(){
        ca.showOriginal();
    }

    private void download(){
        // JFileChooser points to user's default directory
        JFileChooser j = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        // Open the save dialog
        int r = j.showSaveDialog(null);
        if (r == JFileChooser.APPROVE_OPTION)
        {
            try {
                File saveAs = new File(j.getCurrentDirectory().toString() + "\\" + j.getSelectedFile().getName() + ".png");
                ImageIO.write(ca.getOutputImg().bufImg, "png", saveAs);
            } catch (IOException ex) {
                Logger.getLogger(Picture.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void createColorCheckBoxes(){
        File folder = new File(System.getProperty("user.dir") + "\\Colors");
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles == null) return;

        paletteScroll.getVerticalScrollBar().setUnitIncrement(8);

        // Set constraints for checkboxes
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                JCheckBox colorCheck = new JCheckBox(listOfFiles[i].getName());
                colorCheck.setAlignmentY(0.5f);
                colorCheck.setHorizontalAlignment(SwingConstants.LEFT);
                colorCheck.setFont(new Font("Poppins Medium", Font.PLAIN, 11));
                c.gridy = i;
                palettePane.add(colorCheck, c);
                paletteRef.put(colorCheck, listOfFiles[i]);
            }
        }
        // Works as a vertical spacer
        c = new GridBagConstraints();
        JLabel l = new JLabel("");
        c.fill = GridBagConstraints.VERTICAL;
        c.gridy = listOfFiles.length;
        c.weighty = 1;
        palettePane.add(l, c);
    }


    public static void main(String[] args) throws Exception
    {

        try {
//            UIManager.setLookAndFeel(new FlatDarkLaf());
//            FlatNordIJTheme.setup();
            FlatGitHubDarkContrastIJTheme.setup();
        }catch (Exception e){
            e.printStackTrace();
        }
        Interface dialog = new Interface(null);
//        dialog.pack();
//        dialog.setVisible(true);
//        System.exit(0);
    }
}
