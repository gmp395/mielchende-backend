package com.mielchende.product;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/* Carga el catálogo real de Apícola Chende al arrancar, SOLO si la tabla está vacía.
   Así no se pisan los cambios que haga la admin desde el panel.
   Los productos van ordenados por categoría: mieles, otros productos de la colmena
   y productos para el apicultor */
@Component
@Order(3)
public class ProductDataInitializer implements CommandLineRunner {

    /* Carpeta de las fotos de producto (carpeta public del frontend) */
    private static final String IMAGES = "/images/products/";

    /* Información de temporada de los productos vivos */
    private static final String SEASON_INFO = "Consultar disponibilidad en temporada";

    /* Párrafo común a todas las mieles: se escribe una sola vez */
    private static final String RAW_HONEY_NOTE = "\nMiel cruda, tal como la producen las abejas."
            + "\nNuestras mieles no están pasteurizadas, por lo que pueden cristalizar de forma natural"
            + " con el tiempo. La cristalización es un proceso natural propio de la miel y no altera"
            + " su calidad.";

    private static final String CHESTNUT_HONEY_DESCRIPTION = "Miel de castaño, de carácter intenso y sabor"
            + " con personalidad, con notas amaderadas y un punto de amargor característico. Una miel"
            + " apreciada por su aroma marcado y su particular intensidad."
            + RAW_HONEY_NOTE;

    private static final String WILDFLOWER_HONEY_DESCRIPTION = "Miel de mil flores de la cosecha 2026, con"
            + " una composición floral en la que predomina el castaño (57 %), acompañado de zarza (17 %),"
            + " fabáceas (19 %) y otras flores silvestres. Una miel que refleja la diversidad floral de"
            + " nuestro entorno."
            + RAW_HONEY_NOTE;

    private final ProductRepository productRepository;

    public ProductDataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) {
            return;
        }
        productRepository.saveAll(initialProducts());
    }

    /* Catálogo inicial: cada formato de miel es un producto distinto.
       Precio null = "Consultar precio" en la web */
    List<ProductEntity> initialProducts() {
        return List.of(
            /* Mieles */
            product("Miel de castaño", CHESTNUT_HONEY_DESCRIPTION, "500 g", "8.00",
                    "castano-pequeno.jpg", null, ProductCategory.HONEY, ProductStatus.SOLD_OUT),
            product("Miel de castaño", CHESTNUT_HONEY_DESCRIPTION, "1 kg", "15.00",
                    "castano-grande.jpg", null, ProductCategory.HONEY, ProductStatus.SOLD_OUT),
            product("Miel de milflores", WILDFLOWER_HONEY_DESCRIPTION, "500 g", "8.00",
                    "flores-pequeno.jpg", null, ProductCategory.HONEY, ProductStatus.AVAILABLE),
            product("Miel de milflores", WILDFLOWER_HONEY_DESCRIPTION, "1 kg", "15.00",
                    "flores-grande.jpg", null, ProductCategory.HONEY, ProductStatus.AVAILABLE),

            /* Otros productos de la colmena */
            product("Polen",
                    "Pequeños gránulos de polen recogidos por las abejas durante su actividad en las"
                    + " flores. Un producto apícola que destaca por la variedad de colores, aromas y"
                    + " matices que aporta su origen floral.",
                    "250 g", "8.00", "polen.jpg", null,
                    ProductCategory.HIVE_PRODUCTS, ProductStatus.SOLD_OUT),
            product("Tintura de propóleo",
                    "Extracto de propóleo elaborado a partir de esta sustancia resinosa que las abejas"
                    + " recogen de las plantas y utilizan para proteger la colmena. Un producto apícola"
                    + " tradicional, presentado en formato líquido.",
                    "30 ml", null, "propoleo.jpg", null,
                    ProductCategory.HIVE_PRODUCTS, ProductStatus.SOLD_OUT),
            product("Cera",
                    "Cera de abeja natural, obtenida de la actividad de la colmena y presentada en"
                    + " bloques. Un producto versátil, ideal para la elaboración artesanal de velas,"
                    + " cosméticos y otros preparados.",
                    "1 kg", "15.00", "cera.jpg", null,
                    ProductCategory.HIVE_PRODUCTS, ProductStatus.SOLD_OUT),

            /* Productos para el apicultor */
            product("Reina",
                    "Reinas vírgenes jóvenes que todavía no se han apareado, destinadas a la cría y"
                    + " renovación de colonias. Una opción para apicultores que realizan sus propios"
                    + " procesos de fecundación."
                    + "\nReinas fecundadas que ya han realizado el vuelo nupcial y se han apareado."
                    + " Indicadas para la introducción en nuevas colonias o la renovación de reinas"
                    + " en el apiario.",
                    "Unidad", null, "reinas.jpg", SEASON_INFO,
                    ProductCategory.BEEKEEPER_PRODUCTS, ProductStatus.COMING_SOON),
            product("Enjambre",
                    "Enjambre de abeja negra ibérica (Apis mellifera iberiensis), resistente y bien"
                    + " adaptada al clima y la flora autóctona.",
                    "Unidad", null, "enjambre.jpg", SEASON_INFO,
                    ProductCategory.BEEKEEPER_PRODUCTS, ProductStatus.COMING_SOON)
        );
    }

    /* Ayudante para no repetir el Builder en cada producto.
       price: texto con el precio, o null si es "a consultar".
       image: nombre del archivo dentro de la carpeta de fotos */
    private ProductEntity product(String name, String description, String format, String price,
                                  String image, String seasonInfo,
                                  ProductCategory category, ProductStatus status) {
        return ProductEntity.builder()
                .name(name)
                .description(description)
                .format(format)
                .price(price == null ? null : new BigDecimal(price))
                .imageUrl(IMAGES + image)
                .seasonInfo(seasonInfo)
                .category(category)
                .status(status)
                .build();
    }
}