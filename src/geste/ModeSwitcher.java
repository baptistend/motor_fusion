package geste;

import fr.dgac.ivy.IvyException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ModeSwitcher extends JFrame {
    private final RecoGeste recoGeste;

    public ModeSwitcher() throws IvyException {
        // Initialize geste.RecoGeste instance
        recoGeste = new RecoGeste("127.255.255.255:2010");

        // Set up the JFrame
        setTitle("geste.Mode Switcher");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create buttons
        JButton apprentissageButton = new JButton("APPRENTISSAGE");
        JButton reconnaissanceButton = new JButton("RECONNAISSANCE");

        // Add action listeners to buttons
        apprentissageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recoGeste.setMode(Mode.APPRENTISSAGE);
                updateStatusLabel();
            }
        });

        reconnaissanceButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                recoGeste.setMode(Mode.RECONNAISSANCE);
                updateStatusLabel();
            }
        });


        // Status label
        JLabel statusLabel = new JLabel("geste.Mode: " + recoGeste.getMode(), SwingConstants.CENTER);
        add(statusLabel, BorderLayout.NORTH);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        buttonPanel.add(apprentissageButton);
        buttonPanel.add(reconnaissanceButton);
        add(buttonPanel, BorderLayout.CENTER);


        // Show the frame
        setVisible(true);
    }

    // Method to update the status label
    private void updateStatusLabel() {
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new ModeSwitcher();
            } catch (IvyException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
