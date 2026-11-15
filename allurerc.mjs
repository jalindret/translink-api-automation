export default {
  name: "TransLink API Automation Report",
  output: "./allure-report",

  plugins: {
    awesome: {
      options: {
        reportName: "TransLink API Automation",
        singleFile: true,
        reportLanguage: "en",
        groupBy: ["package", "testClass", "testMethod"]
      }
    }
  }
};
