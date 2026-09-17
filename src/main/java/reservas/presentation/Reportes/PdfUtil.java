package reservas.presentation.reportes;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.awt.Desktop;
import java.io.File;

public class PdfUtil {

    public static void generarReporte(
            String titulo,
            String[] columnas,
            Object[][] datos,
            String nombreArchivo) {

        try {

            File carpeta =
                    new File("reportes");

            if (!carpeta.exists()) {
                carpeta.mkdirs();
            }

            File archivo =
                    new File(
                            carpeta,
                            nombreArchivo
                    );

            PdfWriter writer =
                    new PdfWriter(
                            archivo.getAbsolutePath()
                    );

            PdfDocument pdf =
                    new PdfDocument(writer);

            Document document =
                    new Document(pdf);

            document.add(
                    new Paragraph(titulo)
                            .setBold()
                            .setFontSize(16)
            );

            document.add(
                    new Paragraph(" ")
            );

            Table table =
                    new Table(columnas.length);

            // ENCABEZADOS

            for (String columna :
                    columnas) {

                table.addHeaderCell(
                        new Cell()
                                .add(
                                        new Paragraph(
                                                columna
                                        ).setBold()
                                )
                );
            }

            // DATOS

            for (Object[] fila :
                    datos) {

                for (Object valor :
                        fila) {

                    String texto =
                            valor == null
                                    ? ""
                                    : valor.toString();

                    table.addCell(
                            new Cell()
                                    .add(
                                            new Paragraph(
                                                    texto
                                            )
                                    )
                    );
                }
            }

            document.add(table);

            document.close();

            if (Desktop.isDesktopSupported()) {

                Desktop.getDesktop()
                        .open(archivo);
            }

        } catch (Exception e) {

            throw new RuntimeException(
                    "No se pudo generar el PDF: "
                            + e.getMessage(),
                    e
            );
        }
    }
}