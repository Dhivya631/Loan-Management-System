package com.example.loan.controller;

import com.example.loan.dto.LoanSummaryDto;
import com.example.loan.entity.Loan;
import com.example.loan.entity.Officer;
import com.example.loan.entity.User;
import com.example.loan.service.LoanService;
import com.example.loan.service.OfficerService;
import com.example.loan.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private LoanService loanService;
    @Autowired
    private UserService userService;
    @Autowired
    private OfficerService officerService;

    @Autowired
    private ResourceLoader resourceLoader;

    @GetMapping("/loan")
    public String showReportPage(Model model) {
        return "report";
    }
    @GetMapping("/user")
    public String showUserPage(){
        return "userReport";
    }
    @GetMapping("/officer")
    public String getShowOfficerPage(){
        return "officerReport";
    }
    @GetMapping("/loan-summary/pdf")
    public void generatePdfReport(HttpServletResponse response) {
        try {
            List<Loan> loans = loanService.getAllLoans();
            logger.debug("Get all loan details are: "+loans);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(loans);

            InputStream reportStream = getClass().getResourceAsStream("/reports/loan-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=loan-summary.pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
            logger.info("PDF report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating PDF report: ", e);
            throw new RuntimeException("Failed to generate PDF report");
        }
    }
    @GetMapping("/loan-summary/excel")
    public void generateExcelReport(HttpServletResponse response) {
        try {
            List<Loan> loans = loanService.getAllLoans();
            logger.debug("Get all loan details are: "+loans);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(loans);

            InputStream reportStream = getClass().getResourceAsStream("/reports/loan-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=loan-summary.xls");

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);

            exporter.exportReport();
            logger.info("Excel report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating Excel report: ", e);
            throw new RuntimeException("Failed to generate Excel report");
        }
    }
    @GetMapping("/user-summary/pdf")
    public void generateUserPdfReport(HttpServletResponse response) {
        try {
            List<User> user = userService.getAllUser();
            logger.debug("Get all user details are: "+user);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(user);

            InputStream reportStream = getClass().getResourceAsStream("/reports/user-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=user-summary.pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
            logger.info("PDF report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating PDF report: ", e);
            throw new RuntimeException("Failed to generate PDF report");
        }
    }

    @GetMapping("/user-summary/excel")
    public void generateUserExcelReport(HttpServletResponse response) {
        try {
            List<User> user=userService.getAllUser();
            logger.debug("Get all user details are: "+user);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(user);
            InputStream reportStream = getClass().getResourceAsStream("/reports/user-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=user-summary.xls");

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);

            exporter.exportReport();
            logger.info("Excel report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating Excel report: ",
                    e);
            throw new RuntimeException("Failed to generate Excel report");
        }
    }
    @GetMapping("/officer-summary/pdf")
    public void generateOfficerPdfReport(HttpServletResponse response) {
        try {
            List<Officer> officers=officerService.getAllOfficer();
            logger.debug("Get all loan officer details are: "+officers);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(officers);

            InputStream reportStream = getClass().getResourceAsStream("/reports/officer-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=officer-summary.pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
            logger.info("PDF report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating PDF report: ", e);
            throw new RuntimeException("Failed to generate PDF report");
        }
    }

    @GetMapping("/officer-summary/excel")
    public void generateOfficerExcelReport(HttpServletResponse response) {
        try {
            List<Officer> officers = officerService.getAllOfficer();
            logger.debug("Get all loan officer details are: "+officers);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(officers);

            InputStream reportStream = getClass().getResourceAsStream("/reports/officer-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=officer-summary.xls");

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);

            exporter.exportReport();
            logger.info("Excel report generated successfully.");
        } catch (Exception e) {
            logger.error("Error generating Excel report: ",
                    e);
            throw new RuntimeException("Failed to generate Excel report");
        }
    }
    @GetMapping("/amount-summary")
    public String generateLoanSummaryReport(HttpServletResponse response) {
        return "loanAmount";
    }
    @GetMapping("/amount-summary/pdf")
    public void generateLoanAmountPdfReport(HttpServletResponse response) {
        try {
            List<LoanSummaryDto> summary = loanService.getLoanAmountSummary();
            logger.debug("Get all loan summary details are: "+summary);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(summary);

            InputStream reportStream = getClass().getResourceAsStream("/reports/amount-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=amount-summary.pdf");
            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    @GetMapping("/amount-summary/excel")
    public void generateLoanAmountExcelReport(HttpServletResponse response) {
        try {
            List<LoanSummaryDto> summary = loanService.getLoanAmountSummary();
            logger.debug("Get all loan summary details are: "+summary);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(summary);

            InputStream reportStream = getClass().getResourceAsStream("/reports/amount-summary.jrxml");
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("createdBy", "Loan Management System");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=amount-summary.xls");

            JRXlsExporter exporter = new JRXlsExporter();
            exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(response.getOutputStream()));

            SimpleXlsReportConfiguration configuration = new SimpleXlsReportConfiguration();
            configuration.setOnePagePerSheet(true);
            configuration.setRemoveEmptySpaceBetweenRows(true);
            exporter.setConfiguration(configuration);

            exporter.exportReport();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}