package com.icc;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class UI extends JFrame {

    private JPanel mainPanel;
    private JPanel titlePanel;
    private JLabel title;
    private JPanel Prompt1;
    private JPanel top;
    private JLabel p1Label;
    private JButton buttonSelectFile;
    private JPanel bottom;
    private JLabel chosenFile;
    private JPanel Prompt2;
    private JPanel p2top;
    private JLabel p2Label;
    private JButton buttonSelectFolder;
    private JPanel p2bottom;
    private JLabel chosenPath;
    private JPanel Action;
    private JButton buttonConvert;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JLabel statusChange;
    private static String fileNameNoExtension;

    public UI() {
        setContentPane(mainPanel);
        setTitle("Conversor PDF para TXT");
        setSize(460,260);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        buttonSelectFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setDialogTitle("Selecione um arquivo .pdf");

                FileNameExtensionFilter restrict = new FileNameExtensionFilter("Somente arquivos .pdf", "pdf");
                chooser.addChoosableFileFilter(restrict);

                int file = chooser.showOpenDialog(null);

                if (file == JFileChooser.APPROVE_OPTION) {
                    chosenFile.setText(chooser.getSelectedFile().getAbsolutePath());
                    fileNameNoExtension = FilenameUtils.removeExtension(chooser.getSelectedFile().getName());
                } else {
                    chosenFile.setText("Tente novamente.");
                }
            }
        });

        buttonSelectFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int file = chooser.showOpenDialog(null);

                if (file == JFileChooser.APPROVE_OPTION) {
                    File directory = chooser.getSelectedFile();
                    chosenPath.setText(directory.getAbsolutePath());
                } else {
                    chosenPath.setText("Tente novamente.");
                }
            }
        });

        buttonConvert.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonConvert.setEnabled(false);
                statusChange.setText("Processando");
                statusChange.setForeground(Color.ORANGE);

                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        converter(chosenFile.getText(), chosenPath.getText());
                        return null;
                    }

                    @Override
                    protected void done() {
                        buttonConvert.setEnabled(true);
                        statusChange.setText("Concluído");
                        statusChange.setForeground(Color.GREEN);
                    }
                };

                worker.execute();
            }
        });
    }

    public static void main(String[] args) {
        JFrame ui = new UI();
    }

    public void converter(String file, String folder) {
        try {
            File pdfFile = new File(file);
            PDDocument document = PDDocument.load(pdfFile);

            PDFRenderer pdfRenderer = new PDFRenderer(document);

            ITesseract tesseract = new Tesseract();
            tesseract.setDatapath("src/main/resources/tessdata");

            File txtFile = new File(folder + "\\" + fileNameNoExtension +  ".txt");
            FileWriter writer = new FileWriter(txtFile);

            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(pageIndex, 300);
                File tempImageFile = File.createTempFile("temp", ".png");
                ImageIOUtil.writeImage(image, tempImageFile.getPath(), 300);

                String ocrText = tesseract.doOCR(tempImageFile);
                writer.write(ocrText);
                writer.write("\n\n");

                tempImageFile.delete();
            }

            document.close();
            writer.close();
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
        }
    }
}
