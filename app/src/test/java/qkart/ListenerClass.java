package qkart;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import qkart.QkartTests;

public class ListenerClass implements ITestListener {
    String context ;
    public void onStart(ITestContext context) {
        this.context = context.getName() ;

    }
    public void onTestStart(ITestResult result) {
        QkartTests.takeScreenshot(QkartTests.driver,context,result.getName()) ;
    }

    public void onTestSuccess(ITestResult result) {
        QkartTests.takeScreenshot(QkartTests.driver, context,result.getName()) ;
    }

    public void onTestFailure(ITestResult result) {
        QkartTests.takeScreenshot(QkartTests.driver,context,result.getName()) ;
    }


//    public void onFinish(ITestContext context) {
//        ITestListener.super.onFinish(context);
//        System.out.println(context.getPassedTests());
//        System.out.println(context.getFailedTests());
//    }
}
