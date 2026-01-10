package ch.hearc.ig.guideresto.presentation;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.services.EvaluationServices;
import ch.hearc.ig.guideresto.services.RestaurantServices;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.*;

/**
 * @author cedric.baudet
 * @author alain.matile
 */
public class Application {

    private static Scanner scanner;
    private static final Logger logger = LogManager.getLogger(Application.class);
    private static RestaurantServices restaurantServices;
    private static EvaluationServices evaluationServices;

    public static void main(String[] args) {

        scanner = new Scanner(System.in);
        restaurantServices = new RestaurantServices();
        evaluationServices = new EvaluationServices();

        System.out.println("Bienvenue dans GuideResto ! Que souhaitez-vous faire ?");
        int choice;
        do {
            printMainMenu();
            choice = readInt();
            proceedMainMenu(choice);
        } while (choice != 0);

        scanner.close();
        restaurantServices.shutdown(); // close the EntityManager
        evaluationServices.shutdown(); // close the EntityManager
    }

    /**
     * Affichage du menu principal de l'application
     */
    private static void printMainMenu() {
        System.out.println("======================================================");
        System.out.println("Que voulez-vous faire ?");
        System.out.println("1. Afficher la liste de tous les restaurants");
        System.out.println("2. Rechercher un restaurant par son nom");
        System.out.println("3. Rechercher un restaurant par ville");
        System.out.println("4. Rechercher un restaurant par son type de cuisine");
        System.out.println("5. Saisir un nouveau restaurant");
        System.out.println("0. Quitter l'application");
    }

    /**
     * On gère le choix saisi par l'utilisateur
     *
     * @param choice Un nombre entre 0 et 5.
     */
    private static void proceedMainMenu(int choice) {
        switch (choice) {
            case 1:
                showRestaurantsList();
                break;
            case 2:
                searchRestaurantByName();
                break;
            case 3:
                searchRestaurantByCity();
                break;
            case 4:
                searchRestaurantByType();
                break;
            case 5:
                addNewRestaurant();
                break;
            case 0:
                sayGoodBye();
                break;
            default:
                System.out.println("Erreur : saisie incorrecte. Veuillez réessayer");
                break;
        }
    }

    /**
     * On affiche à l'utilisateur une liste de restaurants numérotés, et il doit en sélectionner un !
     *
     * @param restaurants Liste à afficher
     * @return L'instance du restaurant choisi par l'utilisateur
     */
    private static Restaurant pickRestaurant(Set<Restaurant> restaurants) {
        if (restaurants.isEmpty()) { // Si la liste est vide on s'arrête là
            System.out.println("Aucun restaurant n'a été trouvé !");
            return null;
        }

        String result;
        for (Restaurant currentRest : restaurants) {
            result = "";
            result = "\"" + result + currentRest.getName() + "\" - " + currentRest.getAddress().getStreet() + " - ";
            result = result + currentRest.getAddress().getCity().getZipCode() + " " + currentRest.getAddress().getCity().getCityName();
            System.out.println(result);
        }

        System.out.println("Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière");
        String choice = readString();

        return searchRestaurantByName(restaurants, choice);
    }

    /**
     * Affiche la liste de tous les restaurants, sans filtre
     */
    private static void showRestaurantsList() {
        System.out.println("Liste des restaurants : ");
        Restaurant restaurant = pickRestaurant(restaurantServices.findAllRestaurant());

        if (restaurant != null) { // Si l'utilisateur a choisi un restaurant, on l'affiche, sinon on ne fait rien et l'application va réafficher le menu principal
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByName() {
        System.out.println("Veuillez entrer une partie du nom recherché : ");
        String research = readString();

        Set<Restaurant> filteredList = restaurantServices.searchByName(research);
        Restaurant restaurant = pickRestaurant(filteredList);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Affiche une liste de restaurants dont le nom de la ville contient une chaîne de caractères saisie par l'utilisateur
     */
    private static void searchRestaurantByCity() {
        System.out.println("Veuillez entrer une partie du nom de la ville désirée : ");
        String research = readString();

        Set<Restaurant> filteredList = restaurantServices.searchByCity(research);
        Restaurant restaurant = pickRestaurant(filteredList);
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * L'utilisateur choisit une ville parmi celles présentes dans le système.
     *
     * @param cities La liste des villes à présnter à l'utilisateur
     * @return La ville sélectionnée, ou null si aucune ville n'a été choisie.
     */
    private static City pickCity(Set<City> cities) {
        System.out.println("Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée : ");

        for (City currentCity : cities) {
            System.out.println(currentCity.getZipCode() + " " + currentCity.getCityName());
        }
        System.out.println("Entrez \"NEW\" pour créer une nouvelle ville");
        String choice = readString();

        if (choice.equals("NEW")) {
            System.out.println("Veuillez entrer le NPA de la nouvelle ville : ");
            String zipCode = readString();
            System.out.println("Veuillez entrer le nom de la nouvelle ville : ");
            String cityName = readString();
            return restaurantServices.createCity(zipCode, cityName);
        }

        return searchCityByZipCode(cities, choice);
    }

    /**
     * L'utilisateur choisit un type de restaurant parmis ceux présents dans le système.
     *
     * @param types La liste des types de restaurant à présnter à l'utilisateur
     * @return Le type sélectionné, ou null si aucun type n'a été choisi.
     */
    private static RestaurantType pickRestaurantType(Set<RestaurantType> types) {
        System.out.println("Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré : ");
        for (RestaurantType currentType : types) {
            System.out.println("\"" + currentType.getLabel() + "\" : " + currentType.getDescription());
        }
        String choice = readString();

        return searchTypeByLabel(types, choice);
    }

    /**
     * L'utilisateur commence par sélectionner un type de restaurant, puis sélectionne un des restaurants proposés s'il y en a.
     * Si l'utilisateur sélectionne un restaurant, ce dernier lui sera affiché.
     */
    private static void searchRestaurantByType() {
        RestaurantType chosenType = pickRestaurantType(restaurantServices.findAllRestaurantType());

        //TODO : Comment gérer si chosenType est null ? (l'utilisateur a mal saisi le libellé)

        Restaurant restaurant = pickRestaurant(restaurantServices.searchByType(chosenType));
        if (restaurant != null) {
            showRestaurant(restaurant);
        }
    }

    /**
     * Le programme demande les informations nécessaires à l'utilisateur puis crée un nouveau restaurant dans le système.
     */
    private static void addNewRestaurant() {
        System.out.println("Vous allez ajouter un nouveau restaurant !");
        System.out.println("Quel est son nom ?");
        String name = readString();
        System.out.println("Veuillez entrer une courte description : ");
        String description = readString();
        System.out.println("Veuillez entrer l'adresse de son site internet : ");
        String website = readString();
        System.out.println("Rue : ");
        String street = readString();
        City city = null;
        do
        {
            city = pickCity(restaurantServices.findAllCities()); //Sila : une seule transaction dans cette méthode , donc comment faire ?
        } while (city == null);
        RestaurantType restaurantType = null;
        do
        {
            restaurantType = pickRestaurantType(restaurantServices.findAllRestaurantType());
        } while (restaurantType == null);

        //ici on a une nouvelle transaction
        Restaurant restaurant = restaurantServices.createRestaurant(name, description, website, street, city, restaurantType);

        showRestaurant(restaurant);
    }

    /**
     * Affiche toutes les informations du restaurant passé en paramètre, puis affiche le menu des actions disponibles sur ledit restaurant
     *
     * @param restaurant Le restaurant à afficher
     */
    private static void showRestaurant(Restaurant restaurant) {
        System.out.println("Affichage d'un restaurant : ");
        StringBuilder sb = new StringBuilder();
        sb.append(restaurant.getName()).append("\n");
        sb.append(restaurant.getDescription()).append("\n");
        sb.append(restaurant.getType().getLabel()).append("\n");
        sb.append(restaurant.getWebsite()).append("\n");
        sb.append(restaurant.getAddress().getStreet()).append(", ");
        sb.append(restaurant.getAddress().getCity().getZipCode()).append(" ").append(restaurant.getAddress().getCity().getCityName()).append("\n");
        // C'est l'occasion d'utiliser nos namedqueries pour compter en sql plutot que dans la couche de présentation
        sb.append("Nombre de likes : ").append(evaluationServices.countLikes(restaurant, true)).append("\n");
        sb.append("Nombre de dislikes : ").append(evaluationServices.countLikes(restaurant, false)).append("\n");
        sb.append("\nEvaluations reçues : ").append("\n");

        String text;
        for (Evaluation currentEval : restaurant.getEvaluations()) {
            text = getCompleteEvaluationDescription(currentEval);
            if (text != null) { // On va recevoir des null pour les BasicEvaluation donc on ne les traite pas !
                sb.append(text).append("\n");
            }
        }

        System.out.println(sb);

        int choice;
        do { // Tant que l'utilisateur n'entre pas 0 ou 6, on lui propose à nouveau les actions
            showRestaurantMenu();
            choice = readInt();
            proceedRestaurantMenu(choice, restaurant);
        } while (choice != 0 && choice != 6); // 6 car le restaurant est alors supprimé...
    }

    /**
     * Retourne un String qui contient le détail complet d'une évaluation si elle est de type "CompleteEvaluation". Retourne null s'il s'agit d'une BasicEvaluation
     *
     * @param eval L'évaluation à afficher
     * @return Un String qui contient le détail complet d'une CompleteEvaluation, ou null s'il s'agit d'une BasicEvaluation
     */
    private static String getCompleteEvaluationDescription(Evaluation eval) {
        StringBuilder result = new StringBuilder();

        if (eval instanceof CompleteEvaluation) {
            CompleteEvaluation ce = (CompleteEvaluation) eval;
            result.append("Evaluation de : ").append(ce.getUsername()).append("\n");
            result.append("Commentaire : ").append(ce.getComment()).append("\n");
            for (Grade currentGrade : ce.getGrades()) {
                result.append(currentGrade.getCriteria().getName()).append(" : ").append(currentGrade.getGrade()).append("/5").append("\n");
            }
        }

        return result.toString();
    }

    /**
     * Affiche dans la console un ensemble d'actions réalisables sur le restaurant actuellement sélectionné !
     */
    private static void showRestaurantMenu() {
        System.out.println("======================================================");
        System.out.println("Que souhaitez-vous faire ?");
        System.out.println("1. J'aime ce restaurant !");
        System.out.println("2. Je n'aime pas ce restaurant !");
        System.out.println("3. Faire une évaluation complète de ce restaurant !");
        System.out.println("4. Editer ce restaurant");
        System.out.println("5. Editer l'adresse du restaurant");
        System.out.println("6. Supprimer ce restaurant");
        System.out.println("0. Revenir au menu principal");
    }

    /**
     * Traite le choix saisi par l'utilisateur
     *
     * @param choice     Un numéro d'action, entre 0 et 6. Si le numéro ne se trouve pas dans cette plage, l'application ne fait rien et va réafficher le menu complet.
     * @param restaurant L'instance du restaurant sur lequel l'action doit être réalisée
     */
    private static void proceedRestaurantMenu(int choice, Restaurant restaurant) {
        switch (choice) {
            case 1:
                evaluationServices.addBasicEvaluation(restaurant, true);
                System.out.println("Votre vote a été pris en compte !");
                break;
            case 2:
                evaluationServices.addBasicEvaluation(restaurant, false);
                System.out.println("Votre vote a été pris en compte !");
                break;
            case 3:
                evaluateRestaurant(restaurant);
                break;
            case 4:
                editRestaurant(restaurant);
                break;
            case 5:
                editRestaurantAddress(restaurant);
                break;
            case 6:
                deleteRestaurant(restaurant);
                break;
            case 0:
                break;
            default:
                break;
        }
    }

    /**
     * Crée une évaluation complète pour le restaurant. L'utilisateur doit saisir toutes les informations (dont un commentaire et quelques notes)
     *
     * @param restaurant Le restaurant à évaluer
     */
    private static void evaluateRestaurant(Restaurant restaurant) {
        System.out.println("Merci d'évaluer ce restaurant !");
        System.out.println("Quel est votre nom d'utilisateur ? ");
        String username = readString();
        System.out.println("Quel commentaire aimeriez-vous publier ?");
        String comment = readString();

        // L'utilisateur va saisir une note pour chaque critère existant.
        System.out.println("Veuillez svp donner une note entre 1 et 5 pour chacun de ces critères : ");
        Map<EvaluationCriteria, Integer> grades = new HashMap<>();
        for (EvaluationCriteria currentCriteria : evaluationServices.findAllEvaluationCriteria()) {
            System.out.println(currentCriteria.getName() + " : " + currentCriteria.getDescription());
            Integer note = readInt();
            grades.put(currentCriteria, note);
        }
        // on crée tout en une fois, la couche de service gère la transaction (tout pass ou rien)
        if(evaluationServices.addCompleteEvaluation(restaurant, comment, username, grades)){
            System.out.println("Votre évaluation a bien été enregistrée, merci !");

        } else {
            System.out.println("Quelque chose s'est mal passé et votre évaluation n'a pas été enregistrée, veuillez réessayer s.v.p.");
        }
    }

    /**
     * Force l'utilisateur à saisir à nouveau toutes les informations du restaurant (sauf la clé primaire) pour le mettre à jour.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant à modifier
     */
    private static void editRestaurant(Restaurant restaurant) {
        System.out.println("Edition d'un restaurant !");

        System.out.println("Nouveau nom : ");
        String newName = readString();
        System.out.println("Nouvelle description : ");
        String newDescript = readString();
        System.out.println("Nouveau site web : ");
        String newWebSite = readString();
        System.out.println("Nouveau type de restaurant : ");
        RestaurantType newType = pickRestaurantType(restaurantServices.findAllRestaurantType());

        restaurantServices.updateRestaurant(restaurant, newName, newDescript, newWebSite, newType);
        System.out.println("Merci, le restaurant a bien été modifié !");
    }

    /**
     * Permet à l'utilisateur de mettre à jour l'adresse du restaurant.
     * Par soucis de simplicité, l'utilisateur doit tout resaisir.
     *
     * @param restaurant Le restaurant dont l'adresse doit être mise à jour.
     */
    private static void editRestaurantAddress(Restaurant restaurant) {
        System.out.println("Edition de l'adresse d'un restaurant !");

        System.out.println("Nouvelle rue : ");
        String newAdress = readString();
        City newCity = pickCity(restaurantServices.findAllCities());

        try {
            restaurantServices.updateRestaurant(restaurant, newAdress, newCity);
            System.out.println("L'adresse a bien été modifiée ! Merci !");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Après confirmation par l'utilisateur, supprime complètement le restaurant et toutes ses évaluations du référentiel.
     *
     * @param restaurant Le restaurant à supprimer.
     */
    private static void deleteRestaurant(Restaurant restaurant) {
        System.out.println("Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)");
        String choice = readString();
        if (choice.equals("o") || choice.equals("O")) {
            if (restaurantServices.deleteRestaurant(restaurant)){
                System.out.println("Le restaurant a bien été supprimé !");
            } else {
                System.out.println("Quelque chose s'est mal passé, impossible de supprimer le restaurant.");
            }
        }
    }

    /**
     * Recherche dans le Set le restaurant comportant le nom passé en paramètre.
     * Retourne null si le restaurant n'est pas trouvé.
     *
     * @param restaurants Set de restaurants
     * @param name        Nom du restaurant à rechercher
     * @return L'instance du restaurant ou null si pas trouvé
     */
    private static Restaurant searchRestaurantByName(Set<Restaurant> restaurants, String name) {
        for (Restaurant current : restaurants) {
            if (current.getName().equalsIgnoreCase(name)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set la ville comportant le code NPA passé en paramètre.
     * Retourne null si la ville n'est pas trouvée
     *     > Il y a un problème ici, le CP n'est pas unique (ni en vrai ni en DB) et cette fonction retourne
     *     la première occurence trouvée. On ne traitera pas ce point dans ce projet car cela dépasse à mon sens
     *     ce qui est demandé et demande des modifications dans toutes les couches y.c. le modèle de données. De plus,
     *     je suppose que c'est une simplification volontaire. -MW
     *
     * @param cities  Set de villes
     * @param zipCode NPA de la ville à rechercher
     * @return L'instance de la ville ou null si pas trouvé
     */
    private static City searchCityByZipCode(Set<City> cities, String zipCode) {
        for (City current : cities) {
            if (current.getZipCode().equalsIgnoreCase(zipCode)) {
                return current;
            }
        }
        return null;
    }

    /**
     * Recherche dans le Set le type comportant le libellé passé en paramètre.
     * Retourne null si aucun type n'est trouvé.
     *
     * @param types Set de types de restaurant
     * @param label Libellé du type recherché
     * @return L'instance RestaurantType ou null si pas trouvé
     */
    private static RestaurantType searchTypeByLabel(Set<RestaurantType> types, String label) {
        for (RestaurantType current : types) {
            if (current.getLabel().equalsIgnoreCase(label)) {
                return current;
            }
        }
        return null;
    }

    /**
     * readInt ne repositionne pas le scanner au début d'une ligne donc il faut le faire manuellement sinon
     * des problèmes apparaissent quand on demande à l'utilisateur de saisir une chaîne de caractères.
     *
     * @return Un nombre entier saisi par l'utilisateur au clavier
     */
    private static int readInt() {
        int i = 0;
        boolean success = false;
        do { // Tant que l'utilisateur n'aura pas saisi un nombre entier, on va lui demander une nouvelle saisie
            try {
                i = scanner.nextInt();
                success = true;
            } catch (InputMismatchException e) {
                System.out.println("Erreur ! Veuillez entrer un nombre entier s'il vous plaît !");
            } finally {
                scanner.nextLine();
            }

        } while (!success);

        return i;
    }

    /**
     * Méthode readString pour rester consistant avec readInt !
     *
     * @return Une chaîne de caractères saisie par l'utilisateur au clavier
     */
    private static String readString() {
        return scanner.nextLine();
    }

    private static void sayGoodBye() {
        System.out.println("                                 .       .");
        System.out.println("                                / `.   .' \\");
        System.out.println("                        .---.  <    > <    >  .---.");
        System.out.println("                        |    \\  \\ - ~ ~ - /  /    |");
        System.out.println("                         ~-..-~             ~-..-~");
        System.out.println("Au revoir !         \\~~~\\.'                    `./~~~/");
        System.out.println("  \\ |     .-~~^-.    \\__/                        \\__/");
        System.out.println("   \\|   .'  O    \\     /               /       \\  \\");
        System.out.println("       (_____,    `._.'               |         }  \\/~~~/");
        System.out.println("        `----.          /       }     |        /    \\__/");
        System.out.println("              `-.      |       /      |       /      `. ,~~|");
        System.out.println("                  ~-.__|      /_ - ~ ^|      /- _      `..-'   f: f:");
        System.out.println("                       |     /        |     /     ~-.     `-. _||_||_");
        System.out.println("                       |_____|        |_____|         ~ - . _ _ _ _ _>");
        System.out.println("                          credits: Stegosaurus by Michael John Wagoner");
    }

}
