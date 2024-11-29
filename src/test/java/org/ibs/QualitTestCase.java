package org.ibs;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.*;
import java.time.Duration;
import java.util.List;

public class QualitTestCase {
    Connection connection;
    private WebDriver driver;
    private WebDriverWait wait;
    private FoodPage foodPage;

    @BeforeEach
    void testsPreCondition() throws SQLException {
        System.setProperty("webdriver.chromedriver.driver","src/test/resources/chrome.exe");
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("http://localhost:8080/food");
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        foodPage = new FoodPage(driver, wait);
        connection = DriverManager.getConnection("jdbc:h2:tcp://localhost:9092/mem:testdb","user","pass");
    }
    @ParameterizedTest
    @CsvSource({"Картофель,Овощ,false","Melotria,Овощ,true","Клубника,Фрукт,false","Mangosteen,Фрукт,true"})
    void testAddFood(String name, String type, boolean exotic) throws SQLException {
        String query = "SELECT COUNT(FOOD_ID) FROM FOOD WHERE FOOD_NAME = ? AND FOOD_TYPE = ? AND FOOD_EXOTIC = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1,name);
        if (type.equals("Овощ")){
            preparedStatement.setString(2, "VEGETABLE");
        }else{
            preparedStatement.setString(2, "FRUIT");
        }
        int exotic_code = exotic? 1:0;
        preparedStatement.setInt(3, exotic_code);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.first();
        int count = resultSet.getInt("COUNT(FOOD_ID)");
        Assertions.assertEquals(0, count,"Строка уже существует");
        int initialRowCount = foodPage.getRowCount();
        foodPage.addFood(name, type, exotic);
        Assertions.assertEquals(initialRowCount + 1, foodPage.getRowCount(), "Строка не добавилась");
        Assertions.assertEquals(name, foodPage.getLastRowName(), "Неверное название");
        Assertions.assertEquals(type, foodPage.getLastRowType(), "Неверный тип");
        Assertions.assertEquals(String.valueOf(exotic), foodPage.getLastRowExotic(), "Неверная экзотичность");
    }

    @AfterEach
    void postCondition() {
        wait.until(ExpectedConditions.elementToBeClickable(foodPage.getNavbarDropDown()));
        foodPage.getNavbarDropDown().click();
        wait.until(ExpectedConditions.elementToBeClickable(foodPage.getResetButton()));
        foodPage.getResetButton().click();
        wait.until(ExpectedConditions.numberOfElementsToBe(By.xpath("//table/tbody/tr"), 4)); //Более точное ожидание
        Assertions.assertEquals(4, foodPage.getRowCount(), "Ошибка при сбросе данных");
        driver.quit();
    }


    static class FoodPage {
        private final WebDriver driver;
        private final WebDriverWait wait;

        FoodPage(WebDriver driver, WebDriverWait wait) {
            this.driver = driver;
            this.wait = wait;
        }

        WebElement getBtnAdd() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//div[@class = \"btn-grou mt-2 mb-2\"]/button")))); }
        WebElement getBtnSave() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//button[@id=\"save\"]")))); }
        WebElement getInputName() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//input[@id=\"name\"]")))); }
        WebElement getSelectType() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//select[@id=\"type\"]")))); }
        WebElement getInputExotic() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//input[@id=\"exotic\"]")))); }
        List<WebElement> getTableRows() { return driver.findElements(By.xpath("//table/tbody/tr")); }
        WebElement getNavbarDropDown() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//a[@id=\"navbarDropdown\"]"))));}
        WebElement getResetButton() { return wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//a[@id=\"reset\"]"))));}


        int getRowCount() {
            return getTableRows().size();
        }

        String getLastRowName() {
            List<WebElement> rows = getTableRows();
            return rows.get(rows.size() - 1).findElement(By.xpath("./td[1]")).getText();
        }

        String getLastRowType() {
            List<WebElement> rows = getTableRows();
            return rows.get(rows.size() - 1).findElement(By.xpath("./td[2]")).getText();
        }

        String getLastRowExotic() {
            List<WebElement> rows = getTableRows();
            return rows.get(rows.size() - 1).findElement(By.xpath("./td[3]")).getText();
        }

        void addFood(String name, String type, boolean exotic) {
            getBtnAdd().click();
            getInputName().sendKeys(name);
            new Select(getSelectType()).selectByVisibleText(type);
            if (exotic) {
                getInputExotic().click();
            }
            getBtnSave().click();
            wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.xpath("//table/tbody/tr"), getRowCount()));
        }
    }
}