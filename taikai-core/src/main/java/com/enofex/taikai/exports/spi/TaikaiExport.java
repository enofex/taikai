package com.enofex.taikai.exports.spi;

/**
 * An export of a {@link com.enofex.taikai.Taikai} configuration, for example of its rules into a
 * file that coding agents read.
 *
 * <p>Exports run when {@link com.enofex.taikai.Taikai#export()} is called, independently of the
 * rules and checks.</p>
 *
 * <p>Exports are discovered with {@code discoverExports()} through the
 * {@link java.util.ServiceLoader} mechanism. To register an export, list its fully qualified class
 * name in the file {@code META-INF/services/com.enofex.taikai.exports.spi.TaikaiExport}. An export
 * must be a public class with a public constructor without parameters.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public final class RuleCountExport implements TaikaiExport {
 *
 *   @Override
 *   public String description() {
 *     return "Number of rules";
 *   }
 *
 *   @Override
 *   public void export(ExportContext context) {
 *     try {
 *       Files.writeString(context.projectDirectory().resolve("rules.txt"),
 *           context.rules().size() + " rules");
 *     } catch (IOException e) {
 *       throw new UncheckedIOException(e);
 *     }
 *   }
 * }
 * }</pre>
 *
 * @see com.enofex.taikai.Taikai.Builder#discoverExports(java.util.function.Consumer)
 * @see com.enofex.taikai.ExportsConfigurer
 * @see com.enofex.taikai.exports.AgentsMarkdownExport
 */
public interface TaikaiExport {

  /**
   * Returns the description of this export. It names the export in failure messages, for example
   * {@code Number of rules}.
   *
   * @return the description
   */
  String description();

  /**
   * Runs this export, usually by writing a file.
   *
   * <p>If this method throws an exception, {@link com.enofex.taikai.Taikai#export()} stops and
   * throws a {@code TaikaiException} that names this export.</p>
   *
   * @param context the project that is exported
   */
  void export(ExportContext context);
}
