package com.di.jmeter.kafka.config;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.di.jmeter.kafka.utils.VariableSettings;

public class KafkaProducerConfig extends ConfigTestElement
				implements ConfigElement, TestBean, TestStateListener, Serializable {

	private static Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfig.class);
	private static final long serialVersionUID = 3328926106250797599L;

	private KafkaProducer<String, Object> kafkaProducer;
	private List<VariableSettings> extraConfigs;

	private String kafkaBrokers;
	private String batchSize; // dedault: 16384
	private String clientId;
	private String serializerKey;
	private String serializerValue;

	private boolean isSsl;
	private String kafkaSslKeystore; // Kafka ssl keystore (include path information); e.g; "server.keystore.jks"
	private String kafkaSslKeystorePassword; // Keystore Password
	private String kafkaSslTruststore;
	private String kafkaSslTruststorePassword;

	private static final String KAFKA_PRODUCER_CLIENT = "kafkaClient";

	@Override
	public void addConfigElement(ConfigElement config) {

	}

	@Override
	public boolean expectsModification() {
		return false;
	}

	@Override
	public void testStarted() {
		this.setRunningVersion(true);
		TestBeanHelper.prepare(this);
		JMeterVariables variables = getThreadContext().getVariables();

		if (variables.getObject(KAFKA_PRODUCER_CLIENT) != null) {
			LOGGER.error("Kafka Client is already running..");
		} else {
			synchronized (this) {
				try {
					Properties props = new Properties();

					props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, getKafkaBrokers());
					props.put(ProducerConfig.BATCH_SIZE_CONFIG, getBatchSize());
					props.put(ProducerConfig.CLIENT_ID_CONFIG, getClientId());
					props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, getSerializerKey());
					props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, getSerializerValue());

					LOGGER.debug("Additional Cofig Size::: " + getExtraConfigs().size());
					if (getExtraConfigs().size() >= 1) {
						LOGGER.info("Setting up Additional properties");
						for (int i = 0; i < getExtraConfigs().size(); i++) {
							props.put(getExtraConfigs().get(i).getConfigKey(), getExtraConfigs().get(i).getConfigValue());
							LOGGER.debug(String.format("Adding property : %s", getExtraConfigs().get(i).getConfigKey()));
						}
					}

					// check if kafka security protocol is SSL or PLAINTEXT (default)
					LOGGER.info("Kafka SSL properties status: " + getIsSsl());
					if (isSsl == true) {
						LOGGER.info("Setting up Kafka SSL properties");
						props.put("security.protocol", "SSL");
						props.put("ssl.keystore.location", getKafkaSslKeystore());
						props.put("ssl.keystore.password", getKafkaSslKeystorePassword());
						props.put("ssl.truststore.location", getKafkaSslTruststore());
						props.put("ssl.truststore.password", getKafkaSslTruststorePassword());
					}

					kafkaProducer = new KafkaProducer<String, Object>(props);

					// temp
					variables.putObject("di-kafkameter" + ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, getSerializerValue());
					variables.putObject(KAFKA_PRODUCER_CLIENT, kafkaProducer);
					LOGGER.info("Kafka Producer client successfully Initialized");
				} catch (Exception e) {
					LOGGER.error("Error establishing Kafka producer client !!");
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void testStarted(String host) {
		testStarted();
	}

	@Override
	public void testEnded() {
		kafkaProducer.flush();
		kafkaProducer.close();
		LOGGER.info("Kafka Producer client connection terminated");
	}

	@Override
	public void testEnded(String host) {
		testEnded();
	}

	// Getters and setters

	public KafkaProducer<String, Object> getKafkaProducer() {
		return kafkaProducer;
	}

	public void setKafkaProducer(KafkaProducer<String, Object> kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}

	public String getKafkaBrokers() {
		return kafkaBrokers;
	}

	public void setKafkaBrokers(String kafkaBrokers) {
		this.kafkaBrokers = kafkaBrokers;
	}

	public boolean getIsSsl() {
		return isSsl;
	}

	public void setIsSsl(boolean isSsl) {
		this.isSsl = isSsl;
	}

	public String getKafkaSslKeystore() {
		return kafkaSslKeystore;
	}

	public void setKafkaSslKeystore(String kafkaSslKeystore) {
		this.kafkaSslKeystore = kafkaSslKeystore;
	}

	public String getKafkaSslKeystorePassword() {
		return kafkaSslKeystorePassword;
	}

	public void setKafkaSslKeystorePassword(String kafkaSslKeystorePassword) {
		this.kafkaSslKeystorePassword = kafkaSslKeystorePassword;
	}

	public String getKafkaSslTruststore() {
		return kafkaSslTruststore;
	}

	public void setKafkaSslTruststore(String kafkaSslTruststore) {
		this.kafkaSslTruststore = kafkaSslTruststore;
	}

	public String getKafkaSslTruststorePassword() {
		return kafkaSslTruststorePassword;
	}

	public void setKafkaSslTruststorePassword(String kafkaSslTruststorePassword) {
		this.kafkaSslTruststorePassword = kafkaSslTruststorePassword;
	}

	public void setExtraConfigs(List<VariableSettings> extraConfigs) {
		this.extraConfigs = extraConfigs;
	}

	public List<VariableSettings> getExtraConfigs() {
		return this.extraConfigs;
	}

	public String getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(String batchSize) {
		this.batchSize = batchSize;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSerializerKey() {
		return serializerKey;
	}

	public void setSerializerKey(String serializerKey) {
		this.serializerKey = serializerKey;
	}

	public String getSerializerValue() {
		return serializerValue;
	}

	public void setSerializerValue(String serializerValue) {
		this.serializerValue = serializerValue;
	}

	@SuppressWarnings("unchecked")
	public static KafkaProducer<String, Object> getKafkaProducerClient() {
		return (KafkaProducer<String, Object>) JMeterContextService.getContext().getVariables().getObject(KAFKA_PRODUCER_CLIENT);
	}

	public static String getValueSerializer() {
		return JMeterContextService.getContext().getVariables().get("di-kafkameter" + ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);
	}
}
