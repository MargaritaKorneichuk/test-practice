package org.ibs;

import org.ibs.pages.FoodPage;
import org.ibs.utils.Locators;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.*;
import java.time.Duration;
import java.util.List;

/**
 * Класс QualitUITestCase отвечает за проверку UI страницы Список товаров.
 * Тесты проверяют добавление овощей и фруктов,  верифицируя корректность отображения данных в таблице после добавления.
 *  В тестах используется база данных H2 для проверки уникальности добавляемых элементов.
 * @author Корнейчук Маргарита
 */
public class QualitUITestCase {
    Connection connection;
    private WebDriver driver;
    private FoodPage foodPage;
    private WebDriverWait webDriverWait;

    /**
     * Метод, выполняющийся перед каждым тестом. Инициализирует WebDriver,  подключается к базе данных и открывает страницу со списком продуктов.
     * @throws SQLException если возникает ошибка при подключении к базе данных.
     */
    @BeforeEach
    void testsPreCondition() throws SQLException {
        System.setProperty("webdriver.chromedriver.driver","src/test/resources/chrome.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.get("http://localhost:8080/food");
        webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
        foodPage = new FoodPage(driver);
        connection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/mem:testdb","user","pass");
    }

    /**
     * Параметризованный тест для проверки добавления овощей.
     * <p>
     * Шаги выполнения:
     * 1) Проверяется наличие записи с заданными параметрами в базе данных.
     * 2) Запоминается начальное количество строк в таблице.
     * 3) Нажимается кнопка "Добавить".
     * 4) Заполняются поля "Название" и "Экзотичность".
     * 5) Нажимается кнопка "Сохранить".
     * 6) Проверяется, что количество строк в таблице увеличилось на 1.
     * 7) Проверяются значения полей "Название", "Тип" и "Экзотичность" в последней строке таблицы.
     *
     * @param name Наименование товара.
     * @param type Тип товара (для данного теста всегда "Овощ").
     * @param exotic Признак экзотичности товара.
     * @throws SQLException если возникает ошибка при работе с базой данных.
     */
    @ParameterizedTest
    @CsvSource({"Картофель,Овощ,false","Melotria,Овощ,true"})
    void testAddVegetable(String name, String type, boolean exotic) throws SQLException {
        // Проверка на существование записи в базе данных
        String query = "SELECT COUNT(FOOD_ID) FROM FOOD WHERE FOOD_NAME = ? AND FOOD_TYPE = ? AND FOOD_EXOTIC = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,name);
        preparedStatement.setString(2, "VEGETABLE");
        int exotic_code = exotic? 1:0;
        preparedStatement.setInt(3, exotic_code);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.first();
        int count = resultSet.getInt("COUNT(FOOD_ID)");

        //Проверка на существование записи UI
        List<WebElement> table_rows = foodPage.getTableRows();
        WebElement name_r, type_r, exotic_r;
        final boolean[] exist = {false};
        for (WebElement row:table_rows) {
            name_r = row.findElement(Locators.FoodPage.TABLE_ROW_NAME);
            type_r = row.findElement(Locators.FoodPage.TABLE_ROW_TYPE);
            exotic_r = row.findElement(Locators.FoodPage.TABLE_ROW_EXOTIC);
            if (name_r.getText().equals(name)
                    && type_r.getText().equals(type) && exotic_r.getText().equals(String.valueOf(exotic))){
                exist[0] = true;
                break;
            }
        }
        Assertions.assertAll("Проверки на существование строки",
                () -> Assertions.assertFalse(exist[0], "Строка уже существует UI"),
                () -> Assertions.assertEquals(0, count,"Строка уже существует БД"));

        // Добавление товара и проверка количества строк
        int initialRowCount = table_rows.size();
        foodPage = foodPage.clickAddBtn()
                .fillNameField(name)
                .setExotic(exotic)
                .clickSaveBtn();
        webDriverWait.until(ExpectedConditions.elementToBeClickable(Locators.FoodPage.BTN_ADD));
        Assertions.assertEquals(initialRowCount + 1, foodPage.getTableRows().size(), "Строка не добавилась");

        // Проверка данных в последней строке таблицы
        WebElement lastTableRow = foodPage.getTableRows().get(initialRowCount);
        WebElement name_field = lastTableRow.findElement(Locators.FoodPage.TABLE_ROW_NAME);
        WebElement type_field = lastTableRow.findElement(Locators.FoodPage.TABLE_ROW_TYPE);
        WebElement exotic_field = lastTableRow.findElement(Locators.FoodPage.TABLE_ROW_EXOTIC);
        Assertions.assertEquals(name, name_field.getText(), "Неверное название");
        Assertions.assertEquals(type, type_field.getText(), "Неверный тип");
        Assertions.assertEquals(String.valueOf(exotic), exotic_field.getText(), "Неверная экзотичность");
    }
    /**
     * Параметризованный тест для проверки добавления фруктов. Аналогичен testAddVegetable.
     * <p>
     * Шаги выполнения:
     * 1) Проверяется наличие записи с заданными параметрами в базе данных.
     * 2) Запоминается начальное количество строк в таблице.
     * 3) Нажимается кнопка "Добавить".
     * 4) Заполняются поля "Название", "Тип" и "Экзотичность".
     * 5) Нажимается кнопка "Сохранить".
     * 6) Проверяется, что количество строк в таблице увеличилось на 1.
     * 7) Проверяются значения полей "Название", "Тип" и "Экзотичность" в последней строке таблицы.
     *
     * @param name Наименование товара.
     * @param type Тип товара (для данного теста всегда "Фрукт").
     * @param exotic Признак экзотичности товара.
     * @throws SQLException если возникает ошибка при работе с базой данных.
     */
    @ParameterizedTest
    @CsvSource({"Клубника,Фрукт,false","Mangosteen,Фрукт,true"})
    void testAddFruit(String name, String type, boolean exotic) throws SQLException {
        // Проверка на существование записи в базе данных
        String query = "SELECT COUNT(FOOD_ID) FROM FOOD WHERE FOOD_NAME = ? AND FOOD_TYPE = ? AND FOOD_EXOTIC = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,name);
        preparedStatement.setString(2, "FRUIT");
        int exotic_code = exotic? 1:0;
        preparedStatement.setInt(3, exotic_code);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.first();
        int count = resultSet.getInt("COUNT(FOOD_ID)");

        //Проверка на существование записи UI
        List<WebElement> table_rows = foodPage.getTableRows();
        WebElement name_r, type_r, exotic_r;
        final boolean[] exist = {false};
        for (WebElement row:table_rows) {
            name_r = row.findElement(Locators.FoodPage.TABLE_ROW_NAME);
            type_r = row.findElement(Locators.FoodPage.TABLE_ROW_TYPE);
            exotic_r = row.findElement(Locators.FoodPage.TABLE_ROW_EXOTIC);
            if (name_r.getText().equals(name)
                    && type_r.getText().equals(type) && exotic_r.getText().equals(String.valueOf(exotic))){
                exist[0] = true;
                break;
            }
        }
        Assertions.assertAll("Проверки на существование строки",
                () -> Assertions.assertFalse(exist[0],"Строка уже существует UI"),
                () -> Assertions.assertEquals(0, count,"Строка уже существует БД"));


        // Добавление товара и проверка количества строк
        int initialRowCount = foodPage.getTableRows().size();
        foodPage = foodPage.clickAddBtn()
                .fillNameField(name)
                .setType(type)
                .setExotic(exotic)
                .clickSaveBtn();
        webDriverWait.until(ExpectedConditions.elementToBeClickable(Locators.FoodPage.BTN_ADD));
        Assertions.assertEquals(initialRowCount + 1, foodPage.getTableRows().size(), "Строка не добавилась");

        // Проверка данных в последней строке таблицы
        WebElement lastTableRow = foodPage.getTableRows().get(initialRowCount);
        WebElement name_field = lastTableRow.findElement(Locators.FoodPage.TABLE_ROW_NAME);
        WebElement type_field = lastTableRow.findElement(Locators.FoodPage.TABLE_ROW_TYPE);
        WebElement exotic_field = lastTableRow.findElement(Locators.FoodPage.TABLE_ROW_EXOTIC);
        Assertions.assertEquals(name, name_field.getText(), "Неверное название");
        Assertions.assertEquals(type, type_field.getText(), "Неверный тип");
        Assertions.assertEquals(String.valueOf(exotic), exotic_field.getText(), "Неверная экзотичность");
    }
    /**
     * Метод, выполняющийся после каждого теста. Сбрасывает данные на странице, закрывает браузер и соединение с БД.
     */
    @AfterEach
    void postCondition() {
        foodPage.clickNavBarDropDown().clickResetBtn();
        Assertions.assertEquals(4, foodPage.getTableRows().size(), "Ошибка при сбросе данных");
        driver.quit();
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}