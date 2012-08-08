## Reloadable Properties Annotation ##

A simple utilty which allows object fields to be set from properties files via a @ReloadableProperty annotation. 
These properties also auto reload if the given properties file changes during runtime.

### Example Annotation Usage ###
<pre>
	@ReloadableProperty("dynamicProperty.longValue")
	private long primitiveWithDefaultValue = 55;

	@ReloadableProperty("dynamicProperty.substitutionValue")
	private String stringProperty;
</pre>

### Example Properties File ###
<pre>
	dynamicProperty.longValue=12345
	dynamicProperty.substitutionProperty=${dynamicProperty.substitutionValue}
</pre>

### Example Spring XML Configuration ###
* See [spring-reloadableProperties.xml](https://github.com/jamesemorgan/ReloadablePropertiesAnnotation/blob/master/src/main/resources/spring/spring-reloadableProperties.xml) for example configuration
* All main components can be extended or replaced if required

### How it Works  ###
When Spring starts an Application Context an implementation of Springs [PropertySourcesPlaceholderConfigurer](http://static.springsource.org/spring/docs/3.1.x/javadoc-api/org/springframework/context/support/PropertySourcesPlaceholderConfigurer.html) is instantiated to perform additional logic when loading and setting values from a given set of properties files. (see: [ReadablePropertySourcesPlaceholderConfigurer](https://github.com/jamesemorgan/ReloadablePropertiesAnnotation/blob/master/src/main/java/com/morgan/design/properties/internal/ReadablePropertySourcesPlaceholderConfigurer.java))

During the instantiation phasae of an Application Context a new instance of [InstantiationAwareBeanPostProcessorAdapter](http://static.springsource.org/spring/docs/2.5.x/api/org/springframework/beans/factory/config/InstantiationAwareBeanPostProcessorAdapter.html) is also created which allows post bean processing to occur.

Google Guava is used to implement a simple Publish & Subscribe (Pub-Sub) Pattern so that beans can be updated once created, i.e. a bean can subscribe to property change events. (see: [EventBus](http://code.google.com/p/guava-libraries/wiki/EventBusExplained)) 
EventBus was chosen as it is a very easy and simplistic way to implement loosely couple object structure. (see: [blog](http://codingjunkie.net/guava-eventbus/))

When each properties file resource is loaded a [PropertiesWatcher](https://github.com/jamesemorgan/ReloadablePropertiesAnnotation/blob/master/src/main/java/com/morgan/design/properties/internal/PropertiesWatcher.java) is started and attached to the given resource set, reporting on any [java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY](http://docs.oracle.com/javase/7/docs/api/java/nio/file/StandardWatchEventKinds.html#ENTRY_MODIFY) events from the host operating system

When an ENTRY_MODIFY event is fired firstly the resource changed is checked for property value changes then any bean subscribing to changes to the modified property has the specified field value updated with the new property. Once the filed value is updated no other operations are performed on the object.

Each resource specified starts a new thread per parent directory i.e. two properties files in the same directory requires only one ResourceWatcher thread, three properties files in three different directories will start three threads.

### Tests ###
A set of integration and unit tests can be found in _src/test/java_ (tests) & _src/test/resources_ (test resources)

### TODO (Unfinished) ###
* Update test method names
* Creation of any test utilities or helper classes

### Why? ###
* Useful for web applications which often need configuration changes but you don't always want to restart the application before new properties are used.
* Can be used to define several layers of properties which can aid in defining multiple application configurations e.g sandbox/development/testing/production.
* A pet project of mine I have been intending to implement for a while
* A test of the new Java 7 WatchService API
* Another dive in Spring & general investigation of Google Guava's EventBus
* The project is aimed to be open to modification if required
* Sample testing tools (CountDownLatch, Hamcrest-1.3, JMock-2.6.0-RC2)

### Future Changes ###
* Ability to use Spring Expression language to map properties files
* Support for Java 7 Data and Time classes
* Include the ability to define a database driven properties source not just properties files
* Implement error recovery inside PropertiesWatcher.class, including better thread recovery
* Abilty to perform additional re-bind logic when a property is changed, i.e. if a class has an open DB connection whcih needs tobe re-established using newly set properties.
* Replace callback Properties EventHandler with Guava EventBus
* Ability to configure usage via spring's @Configuration 

### Supported Property Type Conversions Available ###
* Joda Time Library (2.1) - [link](http://joda-time.sourceforge.net/)
 * LocalDate.class
 * LocalTime.class
 * LocalDateTime.class
 * Period.class


* Spring Supported (3.1.2-RELEASE)
 * String.class
 * boolean.class, Boolean.class
 * byte.class, Byte.class
 * char.class, Character.class
 * short.class, Short.class
 * int.class, Integer.class
 * long.class,	Long.class
 * float.class, Float.class
 * double.class, Double.class

### Dependencies ###

#### Core ####
* Java 7 SDK
* Spring (3.1.2-RELEASE)
* Google Guava  (12.0)

#### Logging ####
* logback (1.0.6)
* slf4j (1.6.4)

#### Testing ####
* juint (4.10)
* jmock (2.6.0-RC2)
* hamcrest-all (1.3)
* spring-test (3.1.2-RELEASE)