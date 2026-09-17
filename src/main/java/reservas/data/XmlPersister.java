package reservas.data;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class XmlPersister {

    private String path;

    public XmlPersister(String path) {
        this.path = path;
    }

    public Data load() throws Exception {

        File file = new File(path);

        if (!file.exists()) {
            return new Data();
        }

        JAXBContext context =
                JAXBContext.newInstance(Data.class);

        Unmarshaller unmarshaller =
                context.createUnmarshaller();

        try (FileInputStream input =
                     new FileInputStream(file)) {

            return (Data) unmarshaller.unmarshal(input);
        }
    }

    public void store(Data data) throws Exception {

        File file = new File(path);

        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        JAXBContext context =
                JAXBContext.newInstance(Data.class);

        Marshaller marshaller =
                context.createMarshaller();

        marshaller.setProperty(
                Marshaller.JAXB_FORMATTED_OUTPUT,
                true
        );

        try (FileOutputStream output =
                     new FileOutputStream(file)) {

            marshaller.marshal(data, output);
        }
    }
}