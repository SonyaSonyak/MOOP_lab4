package Client;

import DTO.CityDTO;
import DTO.CountryDTO;
import Server.IServerRmi;
import lombok.SneakyThrows;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class Application extends JFrame {
    private static JFrame frame;

    private static IServerRmi backend = null;

    private static CountryDTO currentCountry = null;
    private static CityDTO currentCity = null;

    private static boolean editMode = false;
    private static boolean countryMode = true;

    private static final JButton btnAddCountry = new JButton("Add Country");
    private static final JButton btnAddCity = new JButton("Add City");
    private static final JButton btnEdit = new JButton("Edit Data");
    private static final JButton btnBack = new JButton("Back");
    private static final JButton btnSave = new JButton("Save");
    private static final JButton btnDelete = new JButton("Delete");

    private static final Box menuPanel = Box.createVerticalBox();
    private static final Box actionPanel = Box.createHorizontalBox();
    private static final Box comboPanel = Box.createHorizontalBox();
    private static final Box cityPanel = Box.createVerticalBox();
    private static final Box countryPanel = Box.createVerticalBox();

    private static final JComboBox comboCountry = new JComboBox();
    private static final JComboBox comboCity = new JComboBox();

    private static final JTextField textCountryName = new JTextField(30);
    private static final JTextField textCityName = new JTextField(30);
    private static final JTextField textCityCountryName = new JTextField(30);
    private static final JTextField textCityPopulation = new JTextField(30);

    private Application() {
        super("World Map");
        frame = this;
        frame.setPreferredSize(new Dimension(400, 500));
        frame.setMaximumSize(new Dimension(400, 500));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                frame.dispose();
                System.exit(0);
            }
        });
        Box box = Box.createVerticalBox();
        sizeAllElements();
        frame.setLayout(new FlowLayout());

        // Menu
        menuPanel.add(btnAddCountry);
        menuPanel.add(Box.createVerticalStrut(20));
        btnAddCountry.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                editMode = false;
                countryMode = true;
                menuPanel.setVisible(false);
                comboPanel.setVisible(false);
                countryPanel.setVisible(true);
                cityPanel.setVisible(false);
                actionPanel.setVisible(true);
                btnDelete.setVisible(false);
                pack();
            }
        });
        menuPanel.add(btnAddCity);
        menuPanel.add(Box.createVerticalStrut(20));
        btnAddCity.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                editMode = false;
                countryMode = false;
                menuPanel.setVisible(false);
                comboPanel.setVisible(false);
                countryPanel.setVisible(false);
                cityPanel.setVisible(true);
                actionPanel.setVisible(true);
                btnDelete.setVisible(false);
                pack();
            }
        });
        menuPanel.add(btnEdit);
        menuPanel.add(Box.createVerticalStrut(20));
        btnEdit.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                editMode = true;
                menuPanel.setVisible(false);
                comboPanel.setVisible(true);
                countryPanel.setVisible(false);
                cityPanel.setVisible(false);
                actionPanel.setVisible(true);
                btnDelete.setVisible(true);
                pack();
            }
        });

        // ComboBoxes
        comboPanel.add(new JLabel("Country:"));
        comboPanel.add(comboCountry);
        comboPanel.add(Box.createVerticalStrut(20));
        comboCountry.addActionListener(e -> {
            try {
                currentCountry = backend.countryFindByName((String) comboCountry.getSelectedItem());
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
            countryMode = true;
            countryPanel.setVisible(true);
            cityPanel.setVisible(false);
            fillCountryFields();
            pack();
        });
        comboPanel.add(new JLabel("City:"));
        comboPanel.add(comboCity);
        comboPanel.add(Box.createVerticalStrut(20));
        comboCity.addActionListener(e -> {
            try {
                currentCity = backend.cityFindByName((String) comboCity.getSelectedItem());
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
            countryMode = false;
            countryPanel.setVisible(false);
            cityPanel.setVisible(true);
            try {
                fillCityFields();
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
            pack();
        });
        try {
            fillComboBoxes();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        comboPanel.setVisible(false);

        // City Fields
        cityPanel.add(new JLabel("Name:"));
        cityPanel.add(textCityName);
        cityPanel.add(Box.createVerticalStrut(20));
        cityPanel.add(new JLabel("Country Name:"));
        cityPanel.add(textCityCountryName);
        cityPanel.add(Box.createVerticalStrut(20));
        cityPanel.add(new JLabel("Population:"));
        cityPanel.add(textCityPopulation);
        cityPanel.add(Box.createVerticalStrut(20));
        cityPanel.setVisible(false);

        // Country Fields
        countryPanel.add(new JLabel("Name:"));
        countryPanel.add(textCountryName);
        countryPanel.add(Box.createVerticalStrut(20));
        countryPanel.setVisible(false);

        // Action Bar		
        actionPanel.add(btnSave);
        btnSave.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            public void mouseClicked(MouseEvent event) {
                save();
            }
        });
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(btnDelete);
        btnDelete.addMouseListener(new MouseAdapter() {
            @SneakyThrows
            public void mouseClicked(MouseEvent event) {
                delete();
            }
        });
        actionPanel.add(Box.createVerticalStrut(20));
        actionPanel.add(btnBack);
        actionPanel.add(Box.createVerticalStrut(20));
        btnBack.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                clearFields();
                menuPanel.setVisible(true);
                comboPanel.setVisible(false);
                countryPanel.setVisible(false);
                cityPanel.setVisible(false);
                actionPanel.setVisible(false);
                pack();
            }
        });
        actionPanel.setVisible(false);

        clearFields();
        box.setPreferredSize(new Dimension(300, 500));
        box.add(menuPanel);
        box.add(comboPanel);
        box.add(countryPanel);
        box.add(cityPanel);
        box.add(actionPanel);
        setContentPane(box);
        //pack();
    }

    private static void sizeAllElements() {
        Dimension dimension = new Dimension(300, 30);
        btnAddCountry.setMaximumSize(dimension);
        btnAddCity.setMaximumSize(dimension);
        btnEdit.setMaximumSize(dimension);
        btnBack.setMaximumSize(dimension);
        btnSave.setMaximumSize(dimension);
        btnDelete.setMaximumSize(dimension);

        btnAddCountry.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnAddCity.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSave.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEdit.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnDelete.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension panelDimension = new Dimension(200, 200);
        menuPanel.setMaximumSize(panelDimension);
        comboPanel.setPreferredSize(panelDimension);
        actionPanel.setPreferredSize(panelDimension);
        cityPanel.setPreferredSize(panelDimension);
        countryPanel.setPreferredSize(panelDimension);

        comboCountry.setPreferredSize(dimension);
        comboCity.setPreferredSize(dimension);

        textCityCountryName.setMaximumSize(dimension);
        textCityName.setMaximumSize(dimension);
        textCityPopulation.setMaximumSize(dimension);
        textCountryName.setMaximumSize(dimension);
    }

    private static void save() throws RemoteException {
        if (editMode) {
            if (countryMode) {
                currentCountry.setName(textCountryName.getText());
                if (!backend.countryUpdate(currentCountry)) {
                    JOptionPane.showMessageDialog(null, "Error: update failed!");
                }
            } else {
                currentCity.setName(textCityName.getText());
                currentCity.setPopulation(Long.parseLong(textCityPopulation.getText()));

                CountryDTO country = backend.countryFindByName(textCityCountryName.getText());
                if (country == null) {
                    JOptionPane.showMessageDialog(null, "Error: no such country!");
                    return;
                }
                currentCity.setCountryId(country.getId());

                if (!backend.cityUpdate(currentCity)) {
                    JOptionPane.showMessageDialog(null, "Error: update failed!");
                }
            }
        } else {
            if (countryMode) {
                CountryDTO country = new CountryDTO();
                country.setName(textCountryName.getText());

                if (!backend.countryInsert(country)) {
                    JOptionPane.showMessageDialog(null, "Error: insertion failed!");
                    return;
                }

                comboCountry.addItem(country.getName());
            } else {
                CityDTO city = new CityDTO();
                city.setName(textCityName.getText());
                city.setPopulation(Long.parseLong(textCityPopulation.getText()));

                CountryDTO country = backend.countryFindByName(textCityCountryName.getText());
                if (country == null) {
                    JOptionPane.showMessageDialog(null, "Error: no such country!");
                    return;
                }
                city.setCountryId(country.getId());

                if (!backend.cityInsert(city)) {
                    JOptionPane.showMessageDialog(null, "Error: insertion failed!");
                    return;
                }

                comboCity.addItem(city.getName());
            }
        }
    }

    private static void delete() throws RemoteException {
        if (editMode) {
            if (countryMode) {
                List<CityDTO> list = backend.cityFindByCountryId(currentCountry.getId());
                assert list != null;
                for (CityDTO city : list) {
                    comboCity.removeItem(city.getName());
                    backend.cityDelete(city);
                }
                backend.countryDelete(currentCountry);
                comboCountry.removeItem(currentCountry.getName());

            } else {
                backend.cityDelete(currentCity);
                comboCity.removeItem(currentCity.getName());
            }
        }
    }

    private void fillComboBoxes() throws RemoteException {
        comboCountry.removeAllItems();
        comboCity.removeAllItems();
        List<CountryDTO> countries = backend.countryAll();
        List<CityDTO> cities = backend.cityAll();
        for (CountryDTO country : countries) {
            comboCountry.addItem(country.getName());
        }
        for (CityDTO city : cities) {
            comboCity.addItem(city.getName());
        }
    }

    private static void clearFields() {
        textCountryName.setText("");
        textCityName.setText("");
        textCityCountryName.setText("");
        textCityPopulation.setText("");
        currentCountry = null;
        currentCity = null;
    }

    private static void fillCountryFields() {
        if (currentCountry == null) {
            return;
        }
        textCountryName.setText(currentCountry.getName());
    }

    private static void fillCityFields() throws RemoteException {
        if (currentCity == null) {
            return;
        }
        CountryDTO country = backend.countryFindById(currentCity.getCountryId());
        assert country != null;
        textCityCountryName.setText(country.getName());
        textCityName.setText(currentCity.getName());
        textCityPopulation.setText(String.valueOf(currentCity.getPopulation()));
    }

    public static void main(String[] args) throws IOException, NotBoundException {
        String url = "//localhost:8085/worldmap24";
        backend = (IServerRmi) Naming.lookup(url);
        JFrame myWindow = new Application();
        myWindow.setVisible(true);
    }
}
