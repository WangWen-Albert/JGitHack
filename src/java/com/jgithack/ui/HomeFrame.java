/**
 * JHack.com Inc.
 * Copyright (c) 2004-2016 All Rights Reserved.
 */
package com.jgithack.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import com.jgithack.hack.GitHack;
import com.jgithack.log.LogUtils;

/**
 * Home Frame GUI
 * 
 * @author Albert Wang
 * @version $Id: HomeFrame.java, V0.1 Nov 1, 2016 11:12:20 PM jawangwen@qq.com $
 */
public class HomeFrame extends JFrame {
    private static final long                serialVersionUID = 1L;

    private static final String              BUTTON_HACK      = "Hack";
    private static final String              BUTTON_STOP      = "Stop";

    /** Menu bar */
    private JMenuBar                         menuBar;

    /** Sub menu - help */
    private JMenu                            helpMenu;

    /** Menu option - about */
    private JMenuItem                        aboutItem;

    /** Input field for remote URL */
    private JTextField                       remoteUrlField;

    /** Input field for download directory */
    private JTextField                       downloadField;

    /** Output area like terminal */
    private JTextArea                        terminalArea;

    /** Button to start GitHack */
    private JButton                          hackButton;

    /** Avoid to run GitHack repeatedly when it works */
    private final Lock                       lock             = new ReentrantLock();

    /** thread pool queue for run GitHack asynchronously */
    private volatile BlockingQueue<Runnable> tasks            = new LinkedBlockingQueue<Runnable>();

    /** thread pool for run GitHack asynchronously */
    private volatile ThreadPoolExecutor      executor         = new ThreadPoolExecutor(1, 1, 30,
                                                                  TimeUnit.SECONDS, tasks);

    private volatile GitHack                 gitHack          = new GitHack();

    /**
     * Home GUI Constructor
     */
    public HomeFrame() {
        initMenu();

        downloadField = new JTextField(50);

        remoteUrlField = new JTextField(50);
        remoteUrlField.requestFocus();
        remoteUrlField.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent e) {
                String path = remoteUrlField.getText().trim().replace("http://", "")
                    .replaceFirst("/(\\w*?)\\.(\\w+)$", "");
                downloadField.setText(new File(path).getAbsolutePath());
            }
        });

        terminalArea = new JTextArea(30, 60);
        terminalArea.setEditable(false);
        LogUtils.setPrint(new LogUtils.Print() {
            @Override
            public void toOut(String log) {
                terminalArea.append(log + "\n");
                repaint();
            }

            @Override
            public void toErr(String log) {
                terminalArea.append(log + "\n");
                repaint();
            }
        });

        hackButton = new JButton(BUTTON_HACK);
        hackButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                hackActionPerformed(event);
            }
        });

        JPanel labelPanel = new JPanel(new BorderLayout());
        labelPanel.add(new JLabel("RemoteUrl "), BorderLayout.NORTH);
        labelPanel.add(new JLabel("SaveTo "), BorderLayout.SOUTH);

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(remoteUrlField, BorderLayout.NORTH);
        fieldPanel.add(downloadField, BorderLayout.SOUTH);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        inputPanel.add(labelPanel);
        inputPanel.add(fieldPanel);

        JPanel terminalPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        terminalPanel.add(new JScrollPane(terminalArea));

        JPanel cmdPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cmdPanel.add(hackButton);

        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(inputPanel, BorderLayout.NORTH);
        container.add(terminalPanel, BorderLayout.CENTER);
        container.add(cmdPanel, BorderLayout.SOUTH);

        Dimension dimension = getToolkit().getScreenSize();

        this.setTitle("GitHack V1.0");
        this.setMaximumSize(dimension);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(true);
        this.pack();
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        helpMenu = new JMenu();
        aboutItem = new JMenuItem();

        helpMenu.setText("Help");

        aboutItem.setText("About");
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                aboutActionPerformed(event);
            }
        });
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void hackActionPerformed(ActionEvent event) {
        if (!lock.tryLock()) {
            JOptionPane.showMessageDialog(null, hackButton.getText() + " is doing..");
            return;
        }
        try {
            if (BUTTON_HACK.equals(hackButton.getText())) {
                final String remoteUrl = remoteUrlField.getText().trim();
                if (remoteUrl.length() <= 0) {
                    JOptionPane.showMessageDialog(null, "remoteUrl could not be empty!");
                }

                final String downloadDir = downloadField.getText().trim();

                hackButton.setText(BUTTON_STOP);
                terminalArea.setText("");

                executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, tasks);
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            repaint();
                            gitHack = new GitHack(remoteUrl).withDownloadDir(downloadDir)
                                .nativeFirst(true).skipError(true).withTryTimes(1);
                            gitHack.build().checkout();
                            LogUtils.info("Complete!");
                        } catch (Exception exception) {
                            LogUtils.error(exception, exception.getMessage());
                        } finally {
                            hackButton.setText(BUTTON_HACK);
                        }
                    }
                });
            } else {
                executor.shutdownNow();
                gitHack.close();
                hackButton.setText(BUTTON_HACK);
            }
        } catch (Exception exception) {
            LogUtils.warn(exception, "");
            JOptionPane.showMessageDialog(null, exception.toString());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Show the help/about frame
     * @param evt Event
     */
    private void aboutActionPerformed(ActionEvent event) {
        AboutFrame about = new AboutFrame();
        about.setLocationRelativeTo(this);
        about.setVisible(true);
    }
}