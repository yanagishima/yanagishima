/**
 * - id must be an integer greater than or equal to 0
 * - local storage just stores the id of the announce which user confirmed last time
 *   and an announce having an id greater than the stored id will be considered as unread and shown
 * - text can have html tags and contained url will be auto linked
*/
export default {
  id: 1,
  text: 'test announce'
}
