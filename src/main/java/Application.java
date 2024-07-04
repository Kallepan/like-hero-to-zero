import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

@ManagedBean()
@ApplicationScoped
public class Application {
    static class Country {
        String country_code;
        String country_name;
    }

    /* Basic Setup */
    private static Application instance;
    public String title = "Like Zero To Hero!";
    private EntityManagerFactory emf;

    public static Application getInstance() {
        if (instance == null)
            instance = new Application();

        return instance;
    }

    public Application() {
        instance = this;
        try {
            emf = Persistence.createEntityManagerFactory("someBrokenPersistenceUnit");
        } catch (Exception e) {
            throw new RuntimeException("Failed to create EntityManagerFactory", e);
        }

        // Fetch countries from the database
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT DISTINCT e.country_code, e.country_name FROM emission e");

        // Populate countries list
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
            Country country = new Country();
            country.country_code = (String) result[0];
            country.country_name = (String) result[1];
            countries.add(country);
        }
    }

    /* Login */
    private String username;
    private String password;

    // getters and setters for username and password
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private Scientist activeScientist = null;

    public boolean isLoggedIn() {
        return activeScientist != null;
    }

    /* Emissions */
    public List<Emission> emissions = new LinkedList<Emission>();
    public String activeCountryCode = null;
    private List<Country> countries = new ArrayList<Country>();

    public int year;
    public double amount;
    public String countryCode;

    /** Functionalities */
    public void onSelectCountry(String countryCode) {
        activeCountryCode = countryCode;
        EntityManager em = emf.createEntityManager();
        Query query = em.createQuery("SELECT e FROM emission e WHERE e.country_code = :countryCode");
        query.setParameter("countryCode", countryCode);
        try {
            emissions = query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch emissions", e);
        } finally {
            em.close();
        }
    }

    public void login(String username, String password) {
        EntityManager em = emf.createEntityManager();
        Query query = em
                .createQuery("SELECT s FROM scientist s WHERE s.username = :username AND s.password = :password");
        query.setParameter("username", username);
        query.setParameter("password", password);
        try {
            activeScientist = (Scientist) query.getResultList().get(0);
            if (activeScientist != null) {
                FacesContext.getCurrentInstance().getExternalContext().redirect("manage_emissions.xhtml");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to login", e);
        } finally {
            em.close();
        }
    }

    public void addEmission(String countryCode, double amount, int year) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Emission emission = new Emission();
        emission.setCountryCode(countryCode);
        emission.setCountryName(
                countries.stream().filter(c -> c.country_code.equals(countryCode)).findFirst().get().country_name);
        emission.setEmission(amount);
        emission.setYear(year);
        try {
            em.persist(emission);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new RuntimeException("Failed to add emission", e);
        } finally {
            em.close();
        }
    }
}
