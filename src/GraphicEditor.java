import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

// Главный класс графического редактора
public class GraphicEditor {

    private JFrame frame; // Основное окно программы
    private DrawingPanel drawingPanel; // Панель для рисования
    private JButton createMode, editMode, deleteMode; // Кнопки режимов редактора
    private JLabel statusBar; // Статусная строка

    // Перечисление для режимов редактора: создание, редактирование, удаление, без действия
    public enum Mode {
        CREATE, EDIT, DELETE, NONE
    }

    private Mode currentMode = Mode.NONE; // Текущий режим редактора

    public GraphicEditor() {
        frame = new JFrame("Graphic Editor"); // Создание основного окна

        drawingPanel = new DrawingPanel(); // Инициализация панели рисования

        createMode = new JButton("Создать");
        editMode = new JButton("Редактировать");
        deleteMode = new JButton("Удалить");

        statusBar = new JLabel("Статусная строка");

        // Установка режимов при нажатии на кнопки
        createMode.addActionListener(e -> currentMode = Mode.CREATE);
        editMode.addActionListener(e -> currentMode = Mode.EDIT);
        deleteMode.addActionListener(e -> currentMode = Mode.DELETE);

        // Панель с кнопками режимов
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createMode);
        buttonPanel.add(editMode);
        buttonPanel.add(deleteMode);

        // Добавление компонентов в главное окно
        frame.setLayout(new BorderLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(drawingPanel, BorderLayout.CENTER);
        frame.add(statusBar, BorderLayout.SOUTH);

        // Настройки окна
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    // Панель для рисования линий
    class DrawingPanel extends JPanel {
        private ArrayList<Line> lines = new ArrayList<>(); // Список всех линий
        private Line currentLine; // Текущая линия
        private Line selectedLine; // Выбранная линия для редактирования

        public DrawingPanel() {
            addMouseListener(new MouseAdapter() {
                // Обработчик события клика мыши
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (currentMode == Mode.CREATE) {
                        // Режим создания: начало и конец линии
                        if (currentLine == null) {
                            currentLine = new Line(e.getPoint(), e.getPoint());
                        } else {
                            currentLine.setEndPoint(e.getPoint());
                            lines.add(currentLine);
                            currentLine = null;
                        }
                    } else if (currentMode == Mode.EDIT) {
                        // Режим редактирования: выбор или отмена выбора линии
                        if (selectedLine == null) {
                            selectedLine = findNearestLine(e.getPoint());
                        } else {
                            selectedLine = null;
                        }
                    } else if (currentMode == Mode.DELETE) {
                        // Режим удаления: удаление ближайшей линии
                        Line lineToDelete = findNearestLine(e.getPoint());
                        if (lineToDelete != null) {
                            lines.remove(lineToDelete);
                            repaint();
                        }
                    }
                    repaint();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                // Обработчик движения мыши
                //**********************
                @Override
                public void mouseMoved(MouseEvent e) {
                    if (currentMode == Mode.CREATE && currentLine != null) {
                        currentLine.setEndPoint(e.getPoint());
                        repaint();
                    }

                    if ((currentMode == Mode.EDIT || currentMode == Mode.CREATE) && selectedLine != null) {
                        selectedLine.setEndPoint(e.getPoint());
                        repaint();
                        // Отображение информации о выбранной линии
                        statusBar.setText("Начало: [x=" + selectedLine.getStartPoint().x + " y=" +
                                selectedLine.getStartPoint().y + "], Конец: [x=" + selectedLine.getEndPoint().x +
                                " y=" + selectedLine.getEndPoint().y + "], Длина: " + String.format("%.2f", selectedLine.getLength()));
                        return;
                    }

                    // Отображение статусной строки с уравнением линии или координатами точки
                    Line nearestLine = findNearestLine(e.getPoint());
                    if (nearestLine != null) {
                        // Упрощено ради краткости
                        statusBar.setText("Уравнение линии: A*(" + e.getX() + ") + B*(" + e.getY() + ") + C = 0");
                    } else {
                        statusBar.setText("Точка: (" + e.getX() + ", " + e.getY() + ")");
                    }
                }
                //**************************
                /*@Override
                public void mouseMoved(MouseEvent e) {
                    if (currentMode == Mode.CREATE && currentLine != null) {
                        // В режиме создания обновить конечную точку текущей линии
                        currentLine.setEndPoint(e.getPoint());
                        repaint();
                    }

                    if (currentMode == Mode.EDIT && selectedLine != null) {
                        // В режиме редактирования переместить конечную точку выбранной линии
                        selectedLine.setEndPoint(e.getPoint());
                        repaint();
                    }

                    // Обновление статусной строки с уравнением линии или координатами точки
                    Line nearestLine = findNearestLine(e.getPoint());
                    if (nearestLine != null) {
                        statusBar.setText("Уравнение линии: A*(" + e.getX() + ") + B*(" + e.getY() + ") + C = 0"); // Упрощено ради краткости
                    } else {
                        statusBar.setText("Точка: (" + e.getX() + ", " + e.getY() + ")");
                    }
                }*/
            });
        }

        // Найти ближайшую линию к заданной точке
        private Line findNearestLine(Point p) {
            Line nearestLine = null;
            double minimumDistance = Double.MAX_VALUE;

            for (Line line : lines) {
                double distance = distanceToSegment(line.getStartPoint(), line.getEndPoint(), p);
                if (distance < minimumDistance) {
                    minimumDistance = distance;
                    nearestLine = line;
                }
            }

            if (minimumDistance < 10) { // 10 - пороговое значение
                return nearestLine;
            } else {
                return null;
            }
        }

        // Вычисление расстояния от точки до отрезка
        private double distanceToSegment(Point p1, Point p2, Point p) {
            double norm = Math.sqrt((p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y));
            return Math.abs((p.x - p1.x) * (p2.y - p1.y) - (p.y - p1.y) * (p2.x - p1.x)) / norm;
        }

        // Отображение всех линий на панели
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (Line line : lines) {
                g.setColor(line == selectedLine ? Color.RED : Color.BLACK);
                g.drawLine(line.getStartPoint().x, line.getStartPoint().y, line.getEndPoint().x, line.getEndPoint().y);
            }

            if (currentLine != null) {
                g.drawLine(currentLine.getStartPoint().x, currentLine.getStartPoint().y,
                        currentLine.getEndPoint().x, currentLine.getEndPoint().y);
            }
        }
    }

    // Класс, представляющий линию с начальной и конечной точками
    class Line {
        private Point startPoint, endPoint;

        public Line(Point startPoint, Point endPoint) {
            this.startPoint = startPoint;
            this.endPoint = endPoint;
        }

        public Point getStartPoint() {
            return startPoint;
        }

        public Point getEndPoint() {
            return endPoint;
        }

        public void setEndPoint(Point endPoint) {
            this.endPoint = endPoint;
        }

        public double getLength() {
            return Math.sqrt(Math.pow(endPoint.x - startPoint.x, 2) + Math.pow(endPoint.y - startPoint.y, 2));
        }
    }

    // Главный метод для запуска редактора
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GraphicEditor());
    }
}
