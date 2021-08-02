package ch.keybridge.aws.s3;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.*;

/**
 * A DTO that represents a single node in a file hierarchy: a file of a
 * directory. Fields are named according to how Bootstrap Treeview expects the
 * file hierarchy data:
 * <pre>
 * var tree = [
 *   {
 *     text: "Parent 1",
 *     nodes: [
 *       {
 *         text: "Child 1",
 *         nodes: [
 *           {
 *             text: "Grandchild 1"
 *           },
 *           {
 *             text: "Grandchild 2"
 *           }
 *         ]
 *       },
 *       {
 *         text: "Child 2"
 *       }
 *     ]
 *   },
 *   {
 *     text: "Parent 2"
 *   },
 *   {
 *     text: "Parent 3"
 *   },
 *   {
 *     text: "Parent 4"
 *   },
 *   {
 *     text: "Parent 5"
 *   }
 * ];
 * </pre>
 *
 * @see <a href="https://github.com/jonmiles/bootstrap-treeview">Bootstrap
 * Treeview</a>
 * @author Andrius Druzinis-Vitkus
 * @since 0.0.1 created 2019-02-27
 */
public class S3FileNode {

  /**
   * The file metadata. This field must be null for intermediate directories and
   * non-null for files.
   */
  private S3ObjectSummary s3ObjectSummary;

  /**
   * Mandatory
   * <p>
   * The text value displayed for a given tree node, typically to the right of
   * the nodes icon.
   * <p>
   * The File or directory name.
   */
  private String text;
  /**
   * Child nodes of a directory. Empty for file nodes.
   */
  private final Map<String, S3FileNode> nodes = new LinkedHashMap<>();

  /**
   * Construct a new FileNode instance.
   *
   * @param text the node text (label).
   */
  public S3FileNode(String text) {
    this.text = text;
  }

  /**
   * Get the file metadata.
   *
   * @return File metadata.
   */
  public S3ObjectSummary getS3ObjectSummary() {
    return s3ObjectSummary;
  }

  /**
   * Set the file metadata.
   *
   * @param s3ObjectSummary file metadata.
   */
  public void setS3ObjectSummary(S3ObjectSummary s3ObjectSummary) {
    this.s3ObjectSummary = s3ObjectSummary;
  }

  /**
   * Get file or directory name.
   *
   * @return file or directory name.
   */
  public String getText() {
    return text;
  }

  /**
   * Set the file or directory name.
   *
   * @param text file or directory name.
   */
  public void setText(String text) {
    this.text = text;
  }

  /**
   * Get child nodes.
   *
   * @return child nodes.
   */
  public List<S3FileNode> getNodes() {
    return new ArrayList<>(nodes.values());
  }

  /**
   * Find an existing child node with the specified name or create a new one.
   * <p>
   * This method is not thread-safe. Instances of this class should not be
   * shared across threads.
   *
   * @param name name of a child node
   * @return the child node
   */
  public S3FileNode getOrCreate(String name) {
    return nodes.computeIfAbsent(Objects.requireNonNull(name), S3FileNode::new);
  }

  /**
   * Find an existing child node with the specified name.
   *
   * @param name name of a child node
   * @return the child node
   */
  public S3FileNode get(String name) {
    return nodes.get(Objects.requireNonNull(name));
  }

  /**
   * Find a child node by its text identifier. This recursively searches the
   * root node tree.
   *
   * @param text the text identifier
   * @return the node; null if not found.
   */
  public S3FileNode findNode(String text) {
    return findNode(this, text);
  }

  /**
   * Recursively search the give node tree.
   *
   * @param node the node to search
   * @param text the node identifier
   * @return the matching node
   */
  private S3FileNode findNode(S3FileNode node, String text) {
    S3FileNode found = null;

    if (found == null) {
      for (S3FileNode fileNode : node.getNodes()) {
        if (found != null) {
          break;
        }
        if (!fileNode.getNodes().isEmpty()) {
          found = findNode(fileNode, text);
        }
        if (fileNode.getText().equals(text)) {
          return fileNode;
        }
      }
    }
    return found;
  }
}
