package com.rolandopalermo.facturacion.ec.ride;

import com.rolandopalermo.facturacion.ec.common.XMLUtils;
import com.rolandopalermo.facturacion.ec.common.exception.VeronicaException;
import com.rolandopalermo.facturacion.ec.domain.PaymentMethod;
import com.rolandopalermo.facturacion.ec.domain.ReceiptType;
import com.rolandopalermo.facturacion.ec.domain.Supplier;
import com.rolandopalermo.facturacion.ec.domain.TaxType;
import com.rolandopalermo.facturacion.ec.persistence.PaymentMethodRepository;
import com.rolandopalermo.facturacion.ec.persistence.ReceiptTypeRepository;
import com.rolandopalermo.facturacion.ec.persistence.SupplierRepository;
import com.rolandopalermo.facturacion.ec.persistence.TaxTypeRepository;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("rideGenerator")
public class RIDEGenerator {

    private static final String RUC_IDENTIFICACION_EMISOR = "//infoTributaria/ruc";

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private ReceiptTypeRepository receiptTypeRepository;

    @Autowired
    private TaxTypeRepository taxTypeRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    private HashMap<String, String> hmapFormasPago;
    private HashMap<String, String> hmapTiposDocumentos;
    private HashMap<String, String> hmapTiposImpuestos;

    @PostConstruct
    public void init() {
        List<PaymentMethod> lstPaymentMethods = paymentMethodRepository.findAll();
        List<ReceiptType> lstReceiptTypes = receiptTypeRepository.findAll();
        List<TaxType> lstTaxTypes = taxTypeRepository.findAll();
        hmapFormasPago = (HashMap<String, String>) lstPaymentMethods.stream()
                .collect(Collectors.toMap(PaymentMethod::getCode, PaymentMethod::getDescription));
        hmapTiposDocumentos = (HashMap<String, String>) lstReceiptTypes.stream()
                .collect(Collectors.toMap(ReceiptType::getCode, ReceiptType::getDescription));
        hmapTiposImpuestos = (HashMap<String, String>) lstTaxTypes.stream()
                .collect(Collectors.toMap(TaxType::getCode, TaxType::getDescription));
    }

    public byte[] buildPDF(String xmlContent, String numeroAutorizacion, String fechaAutorizacion)
            throws VeronicaException {
        File comprobante;
        try {
            //Create temp XML file
            comprobante = File.createTempFile(numeroAutorizacion, ".xml");
            Path path = Paths.get(comprobante.getAbsolutePath());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                writer.write(xmlContent);
            }
            //Read XML DOM
            Document doc = XMLUtils.convertStringToDocument(xmlContent);
            String rootElement = XMLUtils.getXmlRootElement(doc);
            String numeroIdentificacion = XMLUtils.xPath(doc, RUC_IDENTIFICACION_EMISOR);
            if (numeroIdentificacion == null || numeroIdentificacion.isEmpty()) {
                throw new VeronicaException(
                        String.format("No se pudo obtener el número de R.U.C. del comprobante %s",
                                xmlContent));
            }
            //Get Logo
            /*Optional<Supplier> empresas = supplierRepository.findByIdNumber(numeroIdentificacion);
            if (!empresas.isPresent()) {
                throw new VeronicaException(
                        String.format("No se pudo obtener el logo del número de R.U.C. %s", numeroIdentificacion));
            }
            byte[] encodedLogo = Base64.getEncoder().encode(empresas.get().getLogo());*/
            //Select template
            StringBuilder sbTemplate = new StringBuilder("/com/rolandopalermo/facturacion/ec/ride/RIDE_");
            sbTemplate.append(rootElement);
            sbTemplate.append(".jrxml");
            String template = sbTemplate.toString();

            InputStream reportStream = RIDEGenerator.class.getResourceAsStream(template);
            JasperReport jasperReport;
            jasperReport = JasperCompileManager.compileReport(reportStream);
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("numeroAutorizacion", numeroAutorizacion);
            parameters.put("fechaAutorizacion", fechaAutorizacion);
            parameters.put("hmapTiposDocumentos", hmapTiposDocumentos);
            parameters.put("hmapTiposImpuestos", hmapTiposImpuestos);
            parameters.put("hmapFormasPago", hmapFormasPago);
            //parameters.put("logo", new String(encodedLogo));
            JRXmlDataSource xmlDataSource = new JRXmlDataSource(comprobante.getAbsolutePath(), rootElement);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, xmlDataSource);
            if (jasperPrint == null) {
                throw new VeronicaException(String.format(
                        "No se pudo generar el PDF para el comprobante con clave de acceso %s", numeroAutorizacion));
            }
            if (!comprobante.delete()) {
                throw new VeronicaException(
                        String.format("No se puede eliminar el archivo temporal en %s", comprobante.getAbsolutePath()));
            }
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (Exception e) {
            e.printStackTrace();
            throw new VeronicaException("Ocurrió un error interno al generar el PDF - " + e.getMessage());
        }
    }

}