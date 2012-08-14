package sage2D.images

import java.awt.Image
import java.awt.MediaTracker
import java.awt.Component
import java.awt.Toolkit
import java.awt.image.FilteredImageSource
import java.awt.image.CropImageFilter

/** 
 * A wrapper class for java.awt.MediaTracker. 
 * It is used to force the rendering component to wait for a set of images to load. 
 * It's especially useful if your application has images that continually use ColorFilters.
 * 
 */
class ImageLoader(val parentComponent : Component) {
    
    /** The MediaTracker wrapped by this class. */
    var mt = new MediaTracker(parentComponent)
    
    /** A unique id for the next image to be tracked. */
    private var nextID : Int = 0
    
    /** Resets the ImageLoader so that it is clean for the next rendering iteration. */
    def reset : Unit = {
        mt = new MediaTracker(parentComponent)
        nextID = 0
    }
    
    /** Adds an image to be tracked by the ImageLoader. */
    def addImage(img : Image) : Unit = {
        mt.addImage(img, nextID)
        nextID += 1
    }
    
    /** 
     * Waits for all images being tracked by this ImageLoader to finish loading, then calls reset. 
     * @return      true if all the tracked images successfully loaded.
     */
    def waitForAll : Boolean = {
        try {
            mt.waitForAll
            reset
            true
        } 
        catch {
            case _ =>
                reset
                false
        }
    }
}

/** Provides static methods for initial image loading. */
object ImageLoader {
    
    /** 
     * Obtains an Image from a file using the current classpath. 
     * This allows the image to be loaded even from inside a jar file.
     */
    def loadPath(path : String) : Image = {
        val imageURL = this.getClass.getClassLoader.getResource(path)
        val image = Toolkit.getDefaultToolkit.getImage(imageURL)
        image
    }
    
    
    /** 
     * Produces an Image from a cropped portion of an existing Image.
     */
    def getCroppedImage(img : Image, x : Int, y : Int, w : Int, h : Int) : Image = {
        val src = img.getSource
        val fis = new FilteredImageSource(src, new CropImageFilter(x,y,w,h))
        Toolkit.getDefaultToolkit.createImage(fis)
    }
}
