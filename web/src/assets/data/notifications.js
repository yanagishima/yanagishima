/**
 * - id must be an integer greater than or equal to 0
 * - items must be sorted by id in descending order
 * - local storage just stores the greatest id when a user last opened the notification modal
 *   and items having an id greater than the stored id will be considered as unread
 * - date is just a string and there are no validations. so any format will be accepted and display in the modal as is
 * - text can have html tags and contained url will be auto linked
*/
export default [
  {
    id: 1,
    date: '2019-07-02',
    title: 'notification title',
    text: 'notification text'
  }
]
