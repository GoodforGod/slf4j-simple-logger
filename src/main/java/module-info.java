module org.slf4j.simple {

    requires org.slf4j;
    requires static io.goodforgod.graalvm.hint.annotations;

    provides org.slf4j.spi.SLF4JServiceProvider with io.goodforgod.slf4j.simplelogger.SimpleServiceProvider;
}
