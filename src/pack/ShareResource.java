/*
 * Copyright © 2009-2010 Novell, Inc.  All Rights Reserved.
 *
 * THIS WORK IS SUBJECT TO U.S. AND INTERNATIONAL COPYRIGHT LAWS AND TREATIES.  IT MAY NOT BE USED, COPIED,
 * DISTRIBUTED, DISCLOSED, ADAPTED, PERFORMED, DISPLAYED, COLLECTED, COMPILED, OR LINKED WITHOUT NOVELL'S
 * PRIOR WRITTEN CONSENT.  USE OR EXPLOITATION OF THIS WORK WITHOUT AUTHORIZATION COULD SUBJECT THE
 * PERPETRATOR TO CRIMINAL AND CIVIL LIABILITY.
 *
 * NOVELL PROVIDES THE WORK "AS IS," WITHOUT ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING WITHOUT THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT. NOVELL, THE
 * AUTHORS OF THE WORK, AND THE OWNERS OF COPYRIGHT IN THE WORK ARE NOT LIABLE FOR ANY CLAIM, DAMAGES,
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT, OR OTHERWISE, ARISING FROM, OUT OF, OR IN
 * CONNECTION WITH THE WORK OR THE USE OR OTHER DEALINGS IN THE WORK.
 */

package org.kablink.teaming.remoting.rest.v1.resource;

import com.sun.jersey.spi.resource.Singleton;
import org.kablink.teaming.ObjectKeys;
import org.kablink.teaming.dao.util.ShareItemSelectSpec;
import org.kablink.teaming.domain.*;
import org.kablink.teaming.domain.Binder;
import org.kablink.teaming.domain.FolderEntry;
import org.kablink.teaming.module.file.FileIndexData;
import org.kablink.teaming.remoting.rest.v1.util.BinderBriefBuilder;
import org.kablink.teaming.remoting.rest.v1.util.ResourceUtil;
import org.kablink.teaming.remoting.rest.v1.util.SearchResultBuilderUtil;
import org.kablink.teaming.remoting.rest.v1.util.UniversalBuilder;
import org.kablink.teaming.rest.v1.model.*;
import org.kablink.teaming.rest.v1.model.Tag;
import org.kablink.teaming.search.SearchUtils;
import org.kablink.teaming.security.AccessControlException;
import org.kablink.util.search.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

/**
 * User: david
 * Date: 7/25/12
 * Time: 2:25 PM
 */
@Path("/shares")
@Singleton
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ShareResource extends AbstractResource {

    @GET
    @Path("/{id}")
    public Share getShare(@PathParam("id") Long id) {
        ShareItem share = _getShareItem(id);
        return ResourceUtil.buildShare(share);
    }

    @POST
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Share updateShare(@PathParam("id") Long id, Share share) {
        ShareItem origItem = _getShareItem(id);
        // You can't change the shared entity or the recipient via this API.  Perhaps I should fail if the client supplies
        // these values and they don't match?
        share.setSharedEntity(new EntityId(origItem.getSharedEntityIdentifier().getEntityId(), origItem.getSharedEntityIdentifier().getEntityType().name(), null));
        share.setRecipient(new EntityId(origItem.getRecipientId(), origItem.getRecipientType().name(), null));
        ShareItem shareItem = toShareItem(share);
        getSharingModule().modifyShareItem(shareItem, id);
        return ResourceUtil.buildShare(shareItem);
    }

    @DELETE
    @Path("/{id}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void deleteShare(@PathParam("id") Long id) {
        _getShareItem(id);
        getSharingModule().deleteShareItem(id);
    }

    @GET
    @Path("/by_user/{id}")
    public SearchResultList<Share> getSharedByUser(@PathParam("id") Long userId) {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedBySpec(userId);
        SearchResultList<Share> results = new SearchResultList<Share>();
        List<ShareItem> shareItems = getShareItems(spec, true);
        for (ShareItem shareItem : shareItems) {
            results.append(ResourceUtil.buildShare(shareItem));
        }
        return results;
    }

    @GET
    @Path("/by_user/{id}/binders")
    public SearchResultList<SharedBinderBrief> getBindersSharedByUser(@PathParam("id") Long userId,
                                                                      @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                      @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden) {
        SearchResultList<SharedBinderBrief> results = new SearchResultList<SharedBinderBrief>();
        results.appendAll(getSharedByBinders(userId, false, true, showHidden, showUnhidden));
        return results;
    }

    @GET
    @Path("/by_user/{id}/binder_tree")
    public BinderTree getSharedByUserBinderTree(@PathParam("id") Long userId,
                                                @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions) {
        SharedBinderBrief [] sharedBinders = getSharedByBinders(userId, false, false, showHidden, showUnhidden);
        return getSubBinderTree(ObjectKeys.SHARED_BY_ME_ID, "/self/shared_by_me", sharedBinders, null, textDescriptions);
    }

    @GET
    @Path("/by_user/{id}/library_folders")
    public SearchResultList<SharedBinderBrief> getLibraryFoldersSharedByUser(@PathParam("id") Long userId,
                                                                             @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                             @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden) {
        SearchResultList<SharedBinderBrief> results = new SearchResultList<SharedBinderBrief>();
        results.appendAll(getSharedByBinders(userId, true, true, showHidden, showUnhidden));
        return results;
    }

    @GET
    @Path("/by_user/{id}/entries")
    public SearchResultList<SharedFolderEntryBrief> getEntriesSharedByUser(@PathParam("id") Long userId,
                                                                           @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                           @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                           @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths) {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedBySpec(userId);
        return _getSharedEntries(ObjectKeys.SHARED_BY_ME_ID, "/self/shared_by_me", spec, null, includeParentPaths, showHidden, showUnhidden);
    }

    @GET
    @Path("/by_user/{id}/files")
    public SearchResultList<FileProperties> getFilesSharedByUser(@PathParam("id") Long userId,
                                                                 @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                 @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                 @QueryParam("file_name") String fileName,
                                                                 @QueryParam("recursive") @DefaultValue("false") boolean recursive,
                                                                 @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths) {
        SearchResultList<FileProperties> results = new SearchResultList<FileProperties>();
        results.appendAll(getSharedByFiles(userId, false, showHidden, showUnhidden));
        if (recursive) {
            results.appendAll(getSubFiles(getSharedByBinders(userId, false, false, showHidden, showUnhidden), fileName, false));
        }
        if (includeParentPaths) {
            populateParentBinderPaths(results);
        }
        return results;
    }

    @GET
    @Path("/by_user/{id}/library_entities")
    public SearchResultList<SearchableObject> getLibraryEntitiesSharedByUser(@PathParam("id") Long userId,
                                                                               @QueryParam("recursive") @DefaultValue("false") boolean recursive,
                                                                               @QueryParam("binders") @DefaultValue("true") boolean includeBinders,
                                                                               @QueryParam("folder_entries") @DefaultValue("true") boolean includeFolderEntries,
                                                                               @QueryParam("files") @DefaultValue("true") boolean includeFiles,
                                                                               @QueryParam("replies") @DefaultValue("true") boolean includeReplies,
                                                                               @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                               @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                               @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths,
                                                                               @QueryParam("keyword") String keyword,
                                                                               @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions,
                                                                               @QueryParam("first") @DefaultValue("0") Integer offset,
                                                                               @QueryParam("count") @DefaultValue("-1") Integer maxCount) {
        _getUser(userId);
        Map<String, Object> nextParams = new HashMap<String, Object>();
        nextParams.put("recursive", Boolean.toString(recursive));
        nextParams.put("binders", Boolean.toString(includeBinders));
        nextParams.put("folder_entries", Boolean.toString(includeFolderEntries));
        nextParams.put("files", Boolean.toString(includeFiles));
        nextParams.put("replies", Boolean.toString(includeReplies));
        nextParams.put("parent_binder_paths", Boolean.toString(includeParentPaths));
        nextParams.put("hidden", Boolean.toString(showHidden));
        nextParams.put("unhidden", Boolean.toString(showUnhidden));
        if (keyword!=null) {
            nextParams.put("keyword", keyword);
        }
        nextParams.put("text_descriptions", Boolean.toString(textDescriptions));
        ShareItemSelectSpec spec = getSharedBySpec(userId);
        SearchResultList<SearchableObject> results = _getLibraryEntities(ObjectKeys.SHARED_BY_ME_ID, null, recursive,
                includeBinders, includeFolderEntries, includeFiles, includeReplies, includeParentPaths, keyword,
                textDescriptions, offset, maxCount, "/with_user/" + userId + "/library_entities", nextParams, spec,
                showHidden, showUnhidden);
        return results;
    }

    @GET
    @Path("/by_user/{id}/library_files")
    public SearchResultList<FileProperties> getLibraryFilesSharedByUser(@PathParam("id") Long userId,
                                                                        @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                        @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                        @QueryParam("file_name") String fileName,
                                                                        @QueryParam("recursive") @DefaultValue("false") boolean recursive,
                                                                        @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths) {
        SearchResultList<FileProperties> results = new SearchResultList<FileProperties>();
        results.appendAll(getSharedByFiles(userId, true, showHidden, showUnhidden));
        if (recursive) {
            results.appendAll(getSubFiles(getSharedByBinders(userId, true, false, showHidden, showUnhidden), fileName, true));
        }
        if (includeParentPaths) {
            populateParentBinderPaths(results);
        }
        return results;
    }

    @GET
    @Path("/by_user/{id}/recent_activity")
   	@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public SearchResultList<RecentActivityEntry> getRecentActivityInSharedByUser(
            @PathParam("id") Long userId,
            @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
            @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
            @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths,
            @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions,
            @QueryParam("first") @DefaultValue("0") Integer offset,
            @QueryParam("count") @DefaultValue("20") Integer maxCount) {
        Map<String, Object> nextParams = new HashMap<String, Object>();
        nextParams.put("parent_binder_paths", Boolean.toString(includeParentPaths));
        nextParams.put("text_descriptions", Boolean.toString(textDescriptions));

        ShareItemSelectSpec spec = getSharedBySpec(userId);
        return _getRecentActivity(ObjectKeys.SHARED_BY_ME_ID, includeParentPaths, textDescriptions, offset, maxCount, spec,
                null, "/shares/by_user/" + userId + "/recent_activity", nextParams, showHidden, showUnhidden);
    }

    @GET
    @Path("/with_user/{id}")
    public SearchResultList<Share> getSharedWithUser(@PathParam("id") Long userId) {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedWithSpec(userId);
        SearchResultList<Share> results = new SearchResultList<Share>();
        List<ShareItem> shareItems = getShareItems(spec, userId, false);
        for (ShareItem shareItem : shareItems) {
            results.append(ResourceUtil.buildShare(shareItem));
        }
        return results;
    }

    @GET
    @Path("/with_user/{id}/binders")
    public SearchResultList<SharedBinderBrief> getBindersSharedWithUser(@PathParam("id") Long userId,
                                                                        @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                        @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden) {
        SearchResultList<SharedBinderBrief> results = new SearchResultList<SharedBinderBrief>();
        results.appendAll(getSharedWithBinders(userId, false, true, showHidden, showUnhidden));
        return results;
    }

    @GET
    @Path("/with_user/{id}/binder_tree")
    public BinderTree getSharedWithUserBinderTree(@PathParam("id") Long userId,
                                                  @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                  @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                  @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions) {
        SharedBinderBrief [] sharedBinders = getSharedWithBinders(userId, false, false, showHidden, showUnhidden);
        return getSubBinderTree(ObjectKeys.SHARED_WITH_ME_ID, "/self/shared_with_me", sharedBinders, null, textDescriptions);
    }

    @GET
    @Path("/with_user/{id}/library_info")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public LibraryInfo getLibraryModTime(@PathParam("id") Long userId) {
        return getSharedWithLibraryInfo(getLoggedInUserId());
    }

    @GET
    @Path("/with_user/{id}/library_folders")
    public SearchResultList<SharedBinderBrief> getLibraryFoldersSharedWithUser(@PathParam("id") Long userId,
                                                                               @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                               @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden) {
        SearchResultList<SharedBinderBrief> results = new SearchResultList<SharedBinderBrief>();
        results.appendAll(getSharedWithBinders(userId, true, true, showHidden, showUnhidden));
        return results;
    }

    @GET
    @Path("/with_user/{id}/library_entities")
    public SearchResultList<SearchableObject> getLibraryEntitiesSharedWithUser(@PathParam("id") Long userId,
                                                      @QueryParam("recursive") @DefaultValue("false") boolean recursive,
                                                      @QueryParam("binders") @DefaultValue("true") boolean includeBinders,
                                                      @QueryParam("folder_entries") @DefaultValue("true") boolean includeFolderEntries,
                                                      @QueryParam("files") @DefaultValue("true") boolean includeFiles,
                                                      @QueryParam("replies") @DefaultValue("true") boolean includeReplies,
                                                      @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                      @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                      @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths,
                                                      @QueryParam("keyword") String keyword,
                                                      @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions,
                                                      @QueryParam("first") @DefaultValue("0") Integer offset,
                                                      @QueryParam("count") @DefaultValue("-1") Integer maxCount) {
        _getUser(userId);
        Map<String, Object> nextParams = new HashMap<String, Object>();
        nextParams.put("recursive", Boolean.toString(recursive));
        nextParams.put("binders", Boolean.toString(includeBinders));
        nextParams.put("folder_entries", Boolean.toString(includeFolderEntries));
        nextParams.put("files", Boolean.toString(includeFiles));
        nextParams.put("replies", Boolean.toString(includeReplies));
        nextParams.put("parent_binder_paths", Boolean.toString(includeParentPaths));
        nextParams.put("hidden", Boolean.toString(showHidden));
        nextParams.put("unhidden", Boolean.toString(showUnhidden));
        if (keyword!=null) {
            nextParams.put("keyword", keyword);
        }
        nextParams.put("text_descriptions", Boolean.toString(textDescriptions));
        ShareItemSelectSpec spec = getSharedWithSpec(userId);
        SearchResultList<SearchableObject> results = _getLibraryEntities(ObjectKeys.SHARED_WITH_ME_ID, userId, recursive,
                includeBinders, includeFolderEntries, includeFiles, includeReplies, includeParentPaths, keyword,
                textDescriptions, offset, maxCount, "/with_user/" + userId + "/library_entities", nextParams, spec,
                showHidden, showUnhidden);
        return results;
    }

    @GET
    @Path("/with_user/{id}/entries")
    public SearchResultList<SharedFolderEntryBrief> getEntriesSharedWithUser(@PathParam("id") Long userId,
                                                                             @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                             @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                             @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths) {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedWithSpec(userId);
        return _getSharedEntries(ObjectKeys.SHARED_WITH_ME_ID, "/self/shared_with_me", spec, userId, includeParentPaths, showHidden, showUnhidden);
    }

    @GET
    @Path("/with_user/{id}/files")
    public SearchResultList<FileProperties> getFilesSharedWithUser(@PathParam("id") Long userId,
                                                                   @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                   @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                   @QueryParam("file_name") String fileName,
                                                                   @QueryParam("recursive") @DefaultValue("false") boolean recursive,
                                                                   @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths) {
        SearchResultList<FileProperties> results = new SearchResultList<FileProperties>();
        results.appendAll(getSharedWithFiles(userId, false, showHidden, showUnhidden));
        if (recursive) {
            results.appendAll(getSubFiles(getSharedWithBinders(userId, false, false, showHidden, showUnhidden), fileName, false));
        }
        if (includeParentPaths) {
            populateParentBinderPaths(results);
        }
        return results;
    }

    @GET
    @Path("/with_user/{id}/library_files")
    public SearchResultList<FileProperties> getLibraryFilesSharedWithUser(@PathParam("id") Long userId,
                                                                          @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                                          @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                                          @QueryParam("file_name") String fileName,
                                                                          @QueryParam("recursive") @DefaultValue("false") boolean recursive,
                                                                          @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths) {
        SearchResultList<FileProperties> results = new SearchResultList<FileProperties>();
        results.appendAll(getSharedWithFiles(userId, true, showHidden, showUnhidden));
        if (recursive) {
            results.appendAll(getSubFiles(getSharedWithBinders(userId, true, false, showHidden, showUnhidden), fileName, true));
        }
        if (includeParentPaths) {
            populateParentBinderPaths(results);
        }
        return results;
    }

    @GET
    @Path("/with_user/{id}/library_tree")
    public BinderTree getSharedWithUserLibraryTree(@PathParam("id") Long userId,
                                                   @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
                                                   @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
                                                   @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions) {
        SharedBinderBrief [] sharedBinders = getSharedWithBinders(userId, true, false, showHidden, showUnhidden);
        return getSubBinderTree(ObjectKeys.SHARED_WITH_ME_ID, "/self/shared_with_me", sharedBinders, buildLibraryTreeCriterion(), textDescriptions);
    }

    @GET
    @Path("/with_user/{id}/recent_activity")
   	@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public SearchResultList<RecentActivityEntry> getRecentActivityInSharedWithUser(
            @PathParam("id") Long userId,
            @QueryParam("hidden") @DefaultValue("false") boolean showHidden,
            @QueryParam("unhidden") @DefaultValue("true") boolean showUnhidden,
            @QueryParam("parent_binder_paths") @DefaultValue("false") boolean includeParentPaths,
            @QueryParam("text_descriptions") @DefaultValue("false") boolean textDescriptions,
            @QueryParam("first") @DefaultValue("0") Integer offset,
            @QueryParam("count") @DefaultValue("20") Integer maxCount) {
        Map<String, Object> nextParams = new HashMap<String, Object>();
        nextParams.put("parent_binder_paths", Boolean.toString(includeParentPaths));
        nextParams.put("text_descriptions", Boolean.toString(textDescriptions));

        ShareItemSelectSpec spec = getSharedWithSpec(userId);
        return _getRecentActivity(ObjectKeys.SHARED_WITH_ME_ID, includeParentPaths, textDescriptions, offset, maxCount,
                spec, userId, "/shares/with_user/" + userId + "/recent_activity", nextParams, showHidden, showUnhidden);
    }

    private SearchResultList<RecentActivityEntry> _getRecentActivity(Long topId, boolean includeParentPaths, boolean textDescriptions,
                                                                  Integer offset, Integer maxCount, ShareItemSelectSpec spec,
                                                                  Long excludedSharerId, String nextUrl, Map<String, Object> nextParams,
                                                                  boolean showHidden, boolean showUnhidden) {
        List<String> binderIds = new ArrayList<String>();
        List<String> entryIds = new ArrayList<String>();
        List<ShareItem> shareItems = getShareItems(spec, excludedSharerId, topId==ObjectKeys.SHARED_BY_ME_ID);
        if (shareItems.size()==0) {
            return new SearchResultList<RecentActivityEntry>();
        }
        for (ShareItem shareItem : shareItems) {
            EntityIdentifier entityId = shareItem.getSharedEntityIdentifier();
            if (entityId.getEntityType()== EntityIdentifier.EntityType.folderEntry) {
                FolderEntry entry = (FolderEntry) getSharingModule().getSharedEntity(shareItem);
                if (showToUser(entry, topId, showHidden, showUnhidden)) {
                    entryIds.add(entityId.getEntityId().toString());
                }
            } else if (entityId.getEntityType()== EntityIdentifier.EntityType.folder || entityId.getEntityType()== EntityIdentifier.EntityType.workspace) {
                Binder binder = (Binder) getSharingModule().getSharedEntity(shareItem);
                if (showBinderToUser(binder, false, topId, showHidden, showUnhidden)) {
                    binderIds.add(entityId.getEntityId().toString());
                }
            }
        }
        Criteria criteria = SearchUtils.entriesForTrackedPlacesEntriesAndPeople(this, binderIds, entryIds, null, true, Constants.LASTACTIVITY_FIELD);
        return _getRecentActivity(includeParentPaths, textDescriptions, offset, maxCount, criteria, nextUrl, nextParams);
    }

    protected SharedBinderBrief [] getSharedByBinders(Long userId, boolean onlyLibrary, boolean replaceParent, boolean showHidden, boolean showUnhidden)  {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedBySpec(userId);
        if (replaceParent) {
            return _getSharedBinders(ObjectKeys.SHARED_BY_ME_ID, "/self/shared_by_me", spec, null, onlyLibrary, showHidden, showUnhidden);
        }
        return _getSharedBinders(null, null, spec, null, onlyLibrary, showHidden, showUnhidden);
    }

    protected SharedFileProperties [] getSharedByFiles(Long userId, boolean onlyLibrary, boolean showHidden, boolean showUnhidden)  {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedBySpec(userId);
        return _getSharedFiles(ObjectKeys.SHARED_BY_ME_ID, "/self/shared_by_me", spec, null, onlyLibrary, showHidden, showUnhidden);
    }

    protected SharedBinderBrief [] getSharedWithBinders(Long userId, boolean onlyLibrary, boolean replaceParent, boolean showHidden, boolean showUnhidden)  {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedWithSpec(userId);
        if (replaceParent) {
            return _getSharedBinders(ObjectKeys.SHARED_WITH_ME_ID, "/self/shared_with_me", spec, userId, onlyLibrary, showHidden, showUnhidden);
        }
        return _getSharedBinders(null, null, spec, userId, onlyLibrary, showHidden, showUnhidden);
    }

    protected SharedFileProperties [] getSharedWithFiles(Long userId, boolean onlyLibrary, boolean showHidden, boolean showUnhidden)  {
        _getUser(userId);
        ShareItemSelectSpec spec = getSharedWithSpec(userId);
        return _getSharedFiles(ObjectKeys.SHARED_WITH_ME_ID, "/self/shared_with_me", spec, userId, onlyLibrary, showHidden, showUnhidden);
    }

    private SearchResultList<SharedFolderEntryBrief> _getSharedEntries(Long topId, String topHref, ShareItemSelectSpec spec, Long excludedSharerId, boolean includeParentPaths, boolean showHidden, boolean showUnhidden) {
        Map<Long, SharedFolderEntryBrief> resultMap = new LinkedHashMap<Long, SharedFolderEntryBrief>();
        List<ShareItem> shareItems = getShareItems(spec, excludedSharerId, topId == ObjectKeys.SHARED_BY_ME_ID);
        for (ShareItem shareItem : shareItems) {
            if (shareItem.getSharedEntityIdentifier().getEntityType()== EntityIdentifier.EntityType.folderEntry) {
                try {
                    FolderEntry entry = (FolderEntry) getSharingModule().getSharedEntity(shareItem);
                    if (showToUser(entry, topId, showHidden, showUnhidden)) {
                        SharedFolderEntryBrief binderBrief = resultMap.get(entry.getId());
                        if (binderBrief!=null) {
                            binderBrief.addShare(ResourceUtil.buildShare(shareItem));
                        } else {
                            binderBrief = ResourceUtil.buildSharedFolderEntryBrief(shareItem, entry);
                            binderBrief.setParentBinder(new ParentBinder(topId, topHref));
                            resultMap.put(entry.getId(), binderBrief);
                        }
                    }
                } catch (AccessControlException e) {
                    logger.warn("User " + getLoggedInUserId() + " does not have permission to read an entity that was shared with him/her: " + shareItem.getEntityTypedId());
                }
            }
        }
        SearchResultList<SharedFolderEntryBrief> results = new SearchResultList<SharedFolderEntryBrief>();
        results.appendAll(resultMap.values());
        if (includeParentPaths) {
            populateParentBinderPaths(results);
        }
        return results;
    }

    protected SharedBinderBrief [] _getSharedBinders(Long topId, String topHref, ShareItemSelectSpec spec, Long excludedSharerId, boolean onlyLibrary, boolean showHidden, boolean showUnhidden)  {
        Map<Long, SharedBinderBrief> resultMap = new LinkedHashMap<Long, SharedBinderBrief>();
        List<ShareItem> shareItems = getShareItems(spec, excludedSharerId, topId == ObjectKeys.SHARED_BY_ME_ID);
        for (ShareItem shareItem : shareItems) {
            if (shareItem.getSharedEntityIdentifier().getEntityType().isBinder()) {
                try {
                    Binder binder = (Binder) getSharingModule().getSharedEntity(shareItem);
                    if (showBinderToUser(binder, onlyLibrary, topId, showHidden, showUnhidden)) {
                        SharedBinderBrief binderBrief = resultMap.get(binder.getId());
                        if (binderBrief!=null) {
                            binderBrief.addShare(ResourceUtil.buildShare(shareItem));
                        } else {
                            binderBrief = ResourceUtil.buildSharedBinderBrief(shareItem, binder);
                            if (topId!=null) {
                                binderBrief.setParentBinder(new ParentBinder(topId, topHref));
                            }
                            resultMap.put(binder.getId(), binderBrief);
                        }
                    }
                } catch (AccessControlException e) {
                    logger.warn("User " + getLoggedInUserId() + " does not have permission to read an entity that was shared with him/her: " + shareItem.getEntityTypedId());
                }
            }
        }
        List<SharedBinderBrief> results = new ArrayList<SharedBinderBrief>();
        results.addAll(resultMap.values());
        return results.toArray(new SharedBinderBrief[results.size()]);
    }

    protected SharedFileProperties [] _getSharedFiles(Long topId, String topHref, ShareItemSelectSpec spec, Long excludedSharerId, boolean onlyLibrary, boolean showHidden, boolean showUnhidden)  {
        Map<String, SharedFileProperties> resultMap = new LinkedHashMap<String, SharedFileProperties>();
        List<ShareItem> shareItems = getShareItems(spec, excludedSharerId, topId==ObjectKeys.SHARED_BY_ME_ID);
        for (ShareItem shareItem : shareItems) {
            if (shareItem.getSharedEntityIdentifier().getEntityType()== EntityIdentifier.EntityType.folderEntry) {
                try {
                    FolderEntry entry = (FolderEntry) getSharingModule().getSharedEntity(shareItem);
                    if (showToUser(entry, topId, showHidden, showUnhidden)) {
                        Set<Attachment> attachments = entry.getAttachments();
                        for (Attachment attachment : attachments) {
                            if (attachment instanceof FileAttachment) {
                                SharedFileProperties fileProps = resultMap.get(attachment.getId());
                                if (fileProps!=null) {
                                    fileProps.addShare(ResourceUtil.buildShare(shareItem));
                                } else {
                                    fileProps = ResourceUtil.buildSharedFileProperties(shareItem, (FileAttachment) attachment);
                                    fileProps.setBinder(new ParentBinder(topId, topHref));
                                    resultMap.put(attachment.getId(), fileProps);
                                }
                            }
                        }
                    }
                } catch (AccessControlException e) {
                    logger.warn("User " + getLoggedInUserId() + " does not have permission to read an entity that was shared with him/her: " + shareItem.getEntityTypedId());
                }
            }
        }
        List<SharedFileProperties> results = new ArrayList<SharedFileProperties>();
        results.addAll(resultMap.values());
        return results.toArray(new SharedFileProperties[results.size()]);
    }

    private boolean showBinderToUser(Binder binder, boolean onlyLibrary, Long collectionId, boolean showHidden, boolean showUnhidden) {
        if (isBinderPreDeleted(binder) || (onlyLibrary && binder.getEntityType() != EntityIdentifier.EntityType.workspace && !binder.isLibrary())) {
            return false;
        }
        if (showHidden && showUnhidden) {
            return true;
        }
        if (!showHidden && !showUnhidden) {
            return false;
        }
        SearchResultList<Tag> entryTags = getBinderTags(binder, true);
        for (Tag tag : entryTags.getResults()) {
            if ((collectionId == ObjectKeys.SHARED_WITH_ME_ID && isHiddenInSharedWithMe(tag)) ||
                    (collectionId == ObjectKeys.SHARED_BY_ME_ID && isHiddenInSharedByMe(tag))) {
                return showHidden;
            }
        }
        return showUnhidden;
    }

    private boolean showToUser(FolderEntry entry, Long collectionId, boolean showHidden, boolean showUnhidden) {
        if (entry.isPreDeleted()) {
            return false;
        }
        if (showHidden && showUnhidden) {
            return true;
        }
        if (!showHidden && !showUnhidden) {
            return false;
        }
        SearchResultList<Tag> entryTags = getEntryTags(entry, true);
        for (Tag tag : entryTags.getResults()) {
            if ((collectionId == ObjectKeys.SHARED_WITH_ME_ID && isHiddenInSharedWithMe(tag)) ||
                    (collectionId == ObjectKeys.SHARED_BY_ME_ID && isHiddenInSharedByMe(tag))) {
                return showHidden;
            }
        }
        return showUnhidden;
    }

    protected BinderTree getSubBinderTree(Long topId, String topHref, SharedBinderBrief [] sharedBinders, Criterion filter, boolean textDescriptions) {
        BinderTree results = new BinderTree();
        if (sharedBinders.length>0) {
            ParentBinder topParent = new ParentBinder(topId, topHref);
            Criteria crit = new Criteria();
            if (filter!=null) {
                crit.add(filter);
            }
            crit.add(Restrictions.eq(Constants.DOC_TYPE_FIELD, Constants.DOC_TYPE_BINDER));
            crit.add(entryAncentryCriterion(sharedBinders));
            Map resultMap = getBinderModule().executeSearchQuery(crit, Constants.SEARCH_MODE_SELF_CONTAINED_ONLY, 0, -1);
            SearchResultBuilderUtil.buildSearchResultsTree(results, sharedBinders, new BinderBriefBuilder(textDescriptions), resultMap);
            results.setItem(null);
            for (SearchResultTreeNode<BinderBrief> child : results.getChildren()) {
                child.getItem().setParentBinder(topParent);
            }
        }
        return results;
    }

    protected List<FileProperties> getSubFiles(SharedBinderBrief [] sharedBinders, String fileName, boolean onlyLibraryFiles) {
        List<FileProperties> results = new ArrayList<FileProperties>();
        if (sharedBinders.length>0) {
            Junction criterion = Restrictions.conjunction()
                    .add(Restrictions.eq(Constants.DOC_TYPE_FIELD, Constants.DOC_TYPE_ATTACHMENT));

            criterion.add(entryAncentryCriterion(sharedBinders));
            if (onlyLibraryFiles) {
                criterion.add(Restrictions.eq(Constants.IS_LIBRARY_FIELD, ((Boolean) onlyLibraryFiles).toString()));
            }
            if (fileName!=null) {
                criterion.add(buildFileNameCriterion(fileName));
            }
            List<FileIndexData> files = getFileModule().getFileDataFromIndex(new Criteria().add(criterion));
            for (FileIndexData file : files) {
                results.add(ResourceUtil.buildFileProperties(file));
            }
        }
        return results;
    }

    private Criterion entryAncentryCriterion(SharedBinderBrief [] sharedBinders) {
        List<String> idList = new ArrayList<String>(sharedBinders.length);
        for (SharedBinderBrief binder : sharedBinders) {
            idList.add(binder.getId().toString());
        }
        return Restrictions.in(Constants.ENTRY_ANCESTRY, idList);
    }

    private SearchResultList<SearchableObject> _getLibraryEntities(Long topId, Long excludedSharerId, boolean recursive,
                                                                   boolean includeBinders, boolean includeFolderEntries,
                                                                   boolean includeFiles, boolean includeReplies,
                                                                   boolean includeParentPaths, String keyword,
                                                                   boolean textDescriptions, Integer offset,
                                                                   Integer maxCount, String nextUrl, Map<String, Object> nextParams,
                                                                   ShareItemSelectSpec spec, boolean showHidden, boolean showUnhidden) {
        List<ShareItem> shareItems = getShareItems(spec, excludedSharerId, topId==ObjectKeys.SHARED_BY_ME_ID);
        SearchResultList<SearchableObject> results = new SearchResultList<SearchableObject>(offset);
        if (shareItems.size()>0) {
            Junction criterion = Restrictions.conjunction();
            Junction searchContext = Restrictions.disjunction();
            for (ShareItem shareItem : shareItems) {
                Junction shareCrit = Restrictions.conjunction();
                if (keyword!=null) {
                    shareCrit.add(buildKeywordCriterion(keyword));
                }
                EntityIdentifier entityId = shareItem.getSharedEntityIdentifier();
                if (entityId.getEntityType()==EntityIdentifier.EntityType.folderEntry) {
                    FolderEntry entry = (FolderEntry) getSharingModule().getSharedEntity(shareItem);
                    if (showToUser(entry, topId, showHidden, showUnhidden)) {
                        shareCrit.add(Restrictions.disjunction()
                                .add(buildEntryCriterion(entityId.getEntityId()))
                                .add(buildAttachmentCriterion(entityId.getEntityId()))
                        );
                    }
                } else if (entityId.getEntityType()==EntityIdentifier.EntityType.folder ||
                        entityId.getEntityType()==EntityIdentifier.EntityType.workspace) {
                    Binder binder = (Binder) getSharingModule().getSharedEntity(shareItem);
                    if (showBinderToUser(binder, false, topId, showHidden, showUnhidden)) {
                        if (recursive) {
                            shareCrit.add(buildSearchBinderCriterion(entityId.getEntityId(), true));
                        } else {
                            shareCrit.add(buildBinderCriterion(entityId.getEntityId()));
                        }
                    }
                }
                searchContext.add(shareCrit);
            }
            criterion.add(searchContext);
            criterion.add(buildDocTypeCriterion(includeBinders, includeFolderEntries, includeFiles, includeReplies));
            criterion.add(buildLibraryCriterion(true));
            Criteria crit = new Criteria();
            crit.add(criterion);
            Map resultsMap = getBinderModule().executeSearchQuery(crit, Constants.SEARCH_MODE_NORMAL, offset, maxCount);
            SearchResultBuilderUtil.buildSearchResults(results, new UniversalBuilder(textDescriptions), resultsMap,
                    nextUrl, nextParams, offset);
        }
        if (includeParentPaths) {
            populateParentBinderPaths(results);
        }
        return results;
    }

    private ShareItem _getShareItem(Long id) {
        ShareItem share = getSharingModule().getShareItem(id);
        if (share.isDeleted() || !share.isLatest() || !share.getSharerId().equals(getLoggedInUserId())) {
            // Don't allow the user to modify a share that is not the latest version of the share, or that was shared
            // by someone else.
            throw new NoShareItemByTheIdException(id);
        }
        return share;
    }
}
