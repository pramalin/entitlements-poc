for cls in DataJpaTest.class WebMvcTest.class AutoConfigureTestDatabase.class JdbcTemplateAutoConfiguration.class; do
     	echo "=== $cls ==="
     	find ~/.m2/repository/org/springframework/boot -name "*.jar" 2>/dev/null | while read -r jar; do
          if unzip -l "$jar" 2>/dev/null | grep -q "$cls"; then
	     echo "$jar"
	  fi
        done
        echo
done
