

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.MapChangeListener;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.file.Paths;
import java.nio.file.Path;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import java.util.stream.Collectors;
/**
 * @author kmontalvo3
 * @version 1.0.0
 * a Music Player
 */
public class MusicPlayer extends Application {


    //filter files by mp3
    private FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
    };


    //getting path to my current java file, and collecting all files which end
    //in Media
    private Path currentRelativePath = Paths.get("");

    private String s = currentRelativePath.toAbsolutePath().toString();

    private File directory = new File(s);

    private File[] listOfFiles = directory.listFiles(filter);

    private List<MP3> listOfMusic  = new ArrayList<>();

    private String selected;

    private String artistTemp;

    private String albumTemp;

    private String titleTemp;

    private String searchCategory;

    private String searchTerm;

    private MediaPlayer selectedMediaPlayer;

    private ObservableList<MP3> items;

    private List<MP3> backingList;



    /**
     * [starts the GUI]
     * @param  primaryStage          [my primary stage]
     * @throws FileNotFoundException [file not found exception]
     * @throws IOException           [ input out put exception]
     */
    @Override
    public void start(Stage primaryStage) throws FileNotFoundException,
    IOException {

        VBox myVbox = new VBox();

        HBox myHbox = new HBox();

        List<MP3> selectedList = new ArrayList<>();

        TableView<MP3> table = new TableView<>();

        table.setEditable(true);

        table.getSelectionModel().selectedItemProperty()
            .addListener(new ChangeListener() {
                @Override
            public void changed(ObservableValue observableValue,
                    Object oldValue, Object newValue) {
    //Check whether item is selected and set value of selected item to Label
                    if (table.getSelectionModel().getSelectedItem() != null) {
                        MP3 selectedM =

                            table.getSelectionModel().getSelectedItem();

                        selectedList.clear();

                        selectedList.add(selectedM);

                        selectedMediaPlayer = new MediaPlayer(
                            selectedM.getMedia());
                    }
                }
            });

        for (File file : listOfFiles) {
            String source = file.toURI().toString();
            Media media  = new Media(source);

            MP3 mp3 = new MP3(media, file);


            ObservableMap<String, Object> metaData = media.getMetadata();


            metaData.addListener(new MapChangeListener<String, Object>() {
                @Override
                public void onChanged(
                    Change<? extends String, ? extends Object> ch) {
                    if (ch.wasAdded()) {
                        if ("artist".equals(ch.getKey())) {
                            mp3.setArtist(metaData.get("artist").toString());
                        } else if ("title".equals(ch.getKey())) {
                            mp3.setTitle(metaData.get("title").toString());
                        } else if ("album".equals(ch.getKey())) {
                            mp3.setAlbum(metaData.get("album").toString());
                        }
                    }
                }
            });
            listOfMusic.add(mp3);
        }

        backingList = listOfMusic;


        Button btPlay = new Button("Play");

        Button btPause = new Button("Pause");

        Button btSearchsongs = new Button("Search Songs");

        Button btShowAllSongs = new Button("Show all Songs");

        btPause.setDisable(true);
        btShowAllSongs.setDisable(true);

        btPlay.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                MediaPlayer m1 = selectedMediaPlayer;
                m1.play();
                btPlay.setDisable(true);
                btPause.setDisable(false);
            }
            });

        btPause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                MediaPlayer m2 = selectedMediaPlayer;
                m2.pause();
                btPlay.setDisable(false);
                btPause.setDisable(true);
            }
            });

        items = FXCollections.observableList(listOfMusic);
        FilteredList<MP3> filteredList = new FilteredList<>(items, p -> true);

        ListView<MP3> listViewOfMedia = new ListView<MP3>(items);


        TableColumn songsColumn = new TableColumn("File Name");
        songsColumn.setCellValueFactory(
            new PropertyValueFactory<MP3, SimpleStringProperty>("file"));

        TableColumn artistColumn = new TableColumn("Artist");
        artistColumn.setCellValueFactory(
            new PropertyValueFactory<MP3, SimpleStringProperty>("artist"));

        TableColumn albumColumn = new TableColumn("Album");
        albumColumn.setCellValueFactory(
            new PropertyValueFactory<MP3, SimpleStringProperty>("album"));

        TableColumn titleColumn = new TableColumn("Title");
        titleColumn.setCellValueFactory(
            new PropertyValueFactory<MP3, SimpleStringProperty>("title"));

        TableColumn attributesColumn = new TableColumn("Attributes");

        attributesColumn.getColumns().addAll(artistColumn, titleColumn,
            albumColumn);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        attributesColumn.setMaxWidth(1f * Integer.MAX_VALUE * 50);


        myHbox.getChildren().addAll(btPlay, btPause, btSearchsongs,
            btShowAllSongs);

        table.setItems(items);

        myVbox.getChildren().addAll(table, myHbox);

        table.getColumns().addAll(songsColumn, attributesColumn);

        myVbox.setFillWidth(true);
        myVbox.setVgrow(table, Priority.ALWAYS);
        Scene scene = new Scene(myVbox, 500, 500);

        primaryStage.setTitle("Music Player");
        primaryStage.setScene(scene);


        primaryStage.show();
        table.refresh();

        btSearchsongs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                btSearchsongs.setDisable(true);
                btShowAllSongs.setDisable(false);

                String[] arrayChoice = {"Artist", "Album",
                    "Title", "File Name"};
                List<String> dialogData;

                dialogData = Arrays.asList(arrayChoice);



                ChoiceDialog dialog = new ChoiceDialog(dialogData.get(0),
                    dialogData);

                dialog.setHeaderText("Select one");
                dialog.setContentText("Choose an attribute");
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()) {
                    dialog.close();
                    searchCategory = result.get();
                    //System.out.println(searchCategory);
                } else {
                    dialog.close();
                }


                TextInputDialog textDialog
                    = new TextInputDialog("type here");
                Optional<String> textresult = textDialog.showAndWait();


                textDialog.setTitle("Search");

                textDialog.setHeaderText("Enter " + searchCategory);







                if (textresult.isPresent()) {
                    searchTerm = textresult.get();
                }

                //System.out.println(searchTerm.toLowerCase());

                if (searchCategory.toLowerCase().equals("artist")) {

                    listOfMusic = listOfMusic.stream()
                    .filter(x ->
                    x.getArtist().toLowerCase().contains(
                        searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
                } else if (searchCategory.toLowerCase().equals("title")) {

                    listOfMusic = listOfMusic.stream()
                    .filter(x ->
                    x.getTitle().toLowerCase().contains(
                        searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
                } else if (searchCategory.toLowerCase().equals("album")) {
                    listOfMusic = listOfMusic.stream()
                    .filter(x ->
                        x.getAlbum() != null).collect(Collectors.toList());
                    listOfMusic = listOfMusic.stream()
                    .filter(x ->
                    x.getAlbum().toLowerCase().contains(
                        searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
                } else if (searchCategory.toLowerCase().equals("file name")) {

                    listOfMusic = listOfMusic.stream()
                    .filter(x ->
                        x.getFile().toLowerCase().contains(
                            searchTerm.toLowerCase()))
                    .collect(Collectors.toList());
                }



                items
                    = FXCollections.observableList(listOfMusic);

                table.setItems(items);
                table.refresh();

            }
        });

        btShowAllSongs.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                btSearchsongs.setDisable(false);
                btShowAllSongs.setDisable(true);

                items
                    = FXCollections.observableList(backingList);



                table.setItems(items);
                table.refresh();

                listOfMusic = backingList;


            }
        });



    }

    /**
     * mp3 class
     */
    public class MP3 {


        private String file;
        private String artist;
        private String album;
        private String title;
        private String selected;
        private Media media;
        private MediaPlayer mediaPlayer;



        /**
         * [makes an mp3]
         * @param  media                 [media]
         * @param  file                  [file of media ]
         * @throws FileNotFoundException [exception thrown when file is absent]
         * @throws IOException           [input output exception]
         */
        public MP3(Media media, File file)
        throws FileNotFoundException, IOException {

            this.media = media;
            this.file = file.getName();
            this.mediaPlayer = new MediaPlayer(media);
        }

        /**
         * [setArtist description]
         * @param artist [the artist of the mp3 file]
         */
        public void setArtist(String artist) {
            this.artist = artist;
        }
        /**
         * [setTitle description]
         * @param title [the title of the mp3 file]
         */
        public void setTitle(String title) {
            this.title = title;
        }
        /**
         * [setAlbum description]
         * @param album [the album the file belongs to]
         */
        public void setAlbum(String album) {
            this.album = album;
        }
        /**
         * [setSelected description]
         * @param selected the selected mp3 to play
         */
        public void setSelected(String selected) {
            this.selected = selected;
        }
        /**
         * [getFile description]
         * @return [a file name]
         */
        public String getFile() {
            return this.file;
            //return this.file;
        }
        /**
         * [getTitle description]
         * @return [the title name]
         */
        public String getTitle() {
            return this.title;
            //return this.title;
        }
        /**
         * [getArtist description]
         * @return [the artist name]
         */
        public String getArtist() {
            return this.artist;
            //return this.artist;
        }
        /**
         * [getAlbum description]
         * @return [the album name]
         */
        public String getAlbum() {
            return this.album;
            //return this.album;
        }
        /**
         * [getSelected description]
         * @return [the slected mp3]
         */
        public String getSelected() {
            return this.selected;
            //return this.album;
        }
        /**
         * [getMedia description]
         * @return [the media]
         */
        public Media getMedia() {
            return this.media;
            //return this.album;
        }
        /**
         * [getMediaPlayer description]
         * @return [the media player belonging to the mp3]
         */
        public MediaPlayer getMediaPlayer() {
            return this.mediaPlayer;
        }

    }

}