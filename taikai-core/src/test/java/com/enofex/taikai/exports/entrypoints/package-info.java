/**
 * Fixtures of the entry points:
 *
 * <ul>
 *   <li>{@code web.ItemController} has 7 handler methods. It inherits {@code items} and
 *   {@code add} from the interface {@code api.ItemsApi}, as an OpenAPI generator writes it, and
 *   {@code health} from the abstract {@code web.BaseController}. Its {@code remove} has the
 *   composed mapping {@code web.Remove}, and {@code search} two HTTP methods.</li>
 *   <li>{@code web.HealthController} is annotated as a controller only through
 *   {@code web.BaseController}, so the component scan of Spring does not find it and it has no
 *   handler methods.</li>
 *   <li>{@code web.TagController} overrides the mapped method of the generic {@code api.CrudApi}
 *   with a mapped method, so the compiler adds a bridge method with its annotations. Its nested
 *   controller, which counts for it, adds POST to the HTTP methods of its handlers.</li>
 *   <li>{@code web.NoteController} implements {@code api.CrudApi} with the mapped method that it
 *   inherits from {@code web.NoteStore}, so its bridge method calls that of the superclass.</li>
 *   <li>{@code web.ReportController} has a mapping for GET through its interface
 *   {@code web.Queries} and one for POST through its superclass {@code web.Commands}, and Spring
 *   combines its handler with the one of the interface, as it searches interfaces first.</li>
 *   <li>{@code web.ItemClient} implements {@code api.ItemsApi} as well, but is no
 *   controller.</li>
 *   <li>{@code app.ItemService}, which {@code web.ItemController} uses, has a {@code @Scheduled}
 *   method.</li>
 * </ul>
 */
package com.enofex.taikai.exports.entrypoints;
