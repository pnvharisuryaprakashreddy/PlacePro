package com.placepro.ui.common;

import com.placepro.model.Notification;
import com.placepro.service.notification.NotificationService;
import com.placepro.util.DateUtil;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * Inbox listing the current user's notifications, newest first.
 * Unread entries are highlighted; clicking an entry marks it as read.
 */
public class NotificationInboxPanel extends JPanel {

    private static final Color UNREAD_BACKGROUND = new Color(232, 240, 254);
    private static final Color READ_FOREGROUND = new Color(97, 97, 97);

    private final NotificationService notificationService;
    private final Runnable onNotificationsChanged;

    private final JComboBox<TypeFilter> typeFilterCombo = new JComboBox<>(new TypeFilter[]{
            new TypeFilter("All", null),
            new TypeFilter("Drive Published", "DRIVE_PUBLISHED"),
            new TypeFilter("Status Change", "STATUS_CHANGE"),
            new TypeFilter("Interview Scheduled", "INTERVIEW_SCHEDULED"),
            new TypeFilter("General", "GENERAL")
    });
    private final JLabel statusLabel = new JLabel(" ");
    private final DefaultListModel<Notification> listModel = new DefaultListModel<>();
    private final JList<Notification> notificationList = new JList<>(listModel);
    private final List<Notification> allNotifications = new ArrayList<>();

    public NotificationInboxPanel(NotificationService notificationService, Runnable onNotificationsChanged) {
        this.notificationService = notificationService;
        this.onNotificationsChanged = onNotificationsChanged == null ? () -> { } : onNotificationsChanged;
        setLayout(new BorderLayout(8, 8));
        buildLayout();
        refresh();
    }

    private void buildLayout() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.add(new JLabel("Type:"));
        typeFilterCombo.addActionListener(event -> applyFilter());
        toolbar.add(typeFilterCombo);
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(event -> refresh());
        toolbar.add(refreshButton);
        toolbar.add(statusLabel);
        add(toolbar, BorderLayout.NORTH);

        notificationList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        notificationList.setCellRenderer(new NotificationCellRenderer());
        notificationList.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                int index = notificationList.locationToIndex(event.getPoint());
                if (index >= 0 && notificationList.getCellBounds(index, index).contains(event.getPoint())) {
                    markAsRead(listModel.getElementAt(index));
                }
            }
        });
        add(new JScrollPane(notificationList), BorderLayout.CENTER);
    }

    public void refresh() {
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText("Loading...");
        UiTasks.run(
                notificationService::getNotificationsForCurrentUser,
                notifications -> {
                    allNotifications.clear();
                    allNotifications.addAll(notifications);
                    applyFilter();
                },
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to load notifications.");
                });
    }

    private void applyFilter() {
        TypeFilter filter = (TypeFilter) typeFilterCombo.getSelectedItem();
        String type = filter == null ? null : filter.type;

        listModel.clear();
        int unread = 0;
        for (Notification notification : allNotifications) {
            if (type != null && !type.equals(notification.getNotificationType())) {
                continue;
            }
            listModel.addElement(notification);
            if (!Boolean.TRUE.equals(notification.getIsRead())) {
                unread++;
            }
        }
        statusLabel.setForeground(javax.swing.UIManager.getColor("Label.foreground"));
        statusLabel.setText(listModel.size() + " notification(s), " + unread + " unread. Click one to mark it as read.");
    }

    private void markAsRead(Notification notification) {
        if (Boolean.TRUE.equals(notification.getIsRead())) {
            return;
        }
        UiTasks.run(
                () -> notificationService.markAsRead(notification.getNotificationId()),
                updated -> {
                    notification.setIsRead(true);
                    notificationList.repaint();
                    applyFilter();
                    onNotificationsChanged.run();
                },
                exception -> {
                    statusLabel.setForeground(UiStyles.ERROR_COLOR);
                    statusLabel.setText("Unable to mark the notification as read.");
                });
    }

    private static final class TypeFilter {
        private final String label;
        private final String type;

        private TypeFilter(String label, String type) {
            this.label = label;
            this.type = type;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class NotificationCellRenderer extends JPanel implements ListCellRenderer<Notification> {

        private final JLabel titleLabel = new JLabel();
        private final JLabel messageLabel = new JLabel();
        private final JLabel metaLabel = new JLabel();

        private NotificationCellRenderer() {
            setLayout(new BorderLayout(2, 2));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(224, 224, 224)),
                    BorderFactory.createEmptyBorder(6, 8, 6, 8)));
            titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
            metaLabel.setFont(metaLabel.getFont().deriveFont(Font.PLAIN, 11f));
            metaLabel.setForeground(READ_FOREGROUND);
            add(titleLabel, BorderLayout.NORTH);
            add(messageLabel, BorderLayout.CENTER);
            add(metaLabel, BorderLayout.SOUTH);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Notification> list,
                                                      Notification notification,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            boolean unread = !Boolean.TRUE.equals(notification.getIsRead());
            titleLabel.setText((unread ? "\u25CF " : "") + notification.getTitle());
            messageLabel.setText("<html>" + escape(notification.getMessage()) + "</html>");
            metaLabel.setText(typeLabel(notification.getNotificationType())
                    + "  |  "
                    + (notification.getCreatedAt() == null ? "-" : DateUtil.formatDateTime(notification.getCreatedAt()))
                    + (unread ? "  |  UNREAD" : ""));

            setBackground(isSelected
                    ? list.getSelectionBackground()
                    : unread ? UNREAD_BACKGROUND : list.getBackground());
            titleLabel.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            messageLabel.setForeground(isSelected
                    ? list.getSelectionForeground()
                    : unread ? list.getForeground() : READ_FOREGROUND);
            setOpaque(true);
            return this;
        }

        private static String typeLabel(String type) {
            if (type == null) {
                return "General";
            }
            switch (type) {
                case "DRIVE_PUBLISHED":
                    return "Drive Published";
                case "STATUS_CHANGE":
                    return "Status Change";
                case "INTERVIEW_SCHEDULED":
                    return "Interview Scheduled";
                default:
                    return "General";
            }
        }

        private static String escape(String text) {
            return text == null
                    ? ""
                    : text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }
    }
}
