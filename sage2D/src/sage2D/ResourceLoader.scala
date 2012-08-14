package sage2D

import java.awt.Toolkit
import java.awt.Image

/** Provides static methods for loading resources. */
object ResourceLoader {
    
    /** 
     * Obtains an Image from a file using the current classpath. 
     * This allows the image to be loaded even from inside a jar file.
     */
    def loadImage(path : String) : Image = {
        val imageURL = this.getClass.getClassLoader.getResource(path)
        val image = Toolkit.getDefaultToolkit.getImage(imageURL)
        image
    }
}